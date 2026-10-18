package com.alviy.app.gmail.mapper

import android.util.Base64
import com.alviy.app.gmail.model.GmailMessage
import com.alviy.app.gmail.model.GmailReceiptTransaction
import com.alviy.app.gmail.model.MessagePart

private const val NUMBER = """[0-9]{1,3}(?:[.,][0-9]{3})*(?:[.,][0-9]{1,2})?|[0-9]+(?:[.,][0-9]{1,2})?"""
private const val CURRENCY = """Rp\.?|IDR|USD|\$|EUR|€|SGD"""
private val AMOUNT_RE = Regex("""($CURRENCY)\s?($NUMBER)""", RegexOption.IGNORE_CASE)
private val TOTAL_RE = Regex(
    """(?:grand\s*total|total\s*(?:bayar|pembayaran|payment)?|jumlah|amount\s*paid)[^0-9A-Za-z]{0,25}($CURRENCY)\s?($NUMBER)""",
    RegexOption.IGNORE_CASE
)

fun GmailMessage.toReceipt(): GmailReceiptTransaction {
    val subject = header("Subject").orEmpty()
    val body = payload?.extractText().orEmpty()
    val (amount, currency) = parseAmount("$subject\n$body")
    return GmailReceiptTransaction(
        id = id,
        merchant = header("From")?.let(::displayName) ?: "Unknown",
        subject = subject,
        amount = amount,
        currency = currency,
        dateMillis = internalDate?.toLongOrNull() ?: 0L,
        snippet = snippet.orEmpty(),
    )
}

private fun GmailMessage.header(name: String) =
    payload?.headers?.firstOrNull { it.name.equals(name, ignoreCase = true) }?.value

/** "Tokopedia <no-reply@tokopedia.com>" -> "Tokopedia" */
private fun displayName(from: String): String =
    from.substringBefore("<").trim().trim('"').ifBlank { from.substringAfter("<").substringBefore(">") }

/** Walk the MIME tree: prefer text/plain, fall back to text/html with tags stripped. */
private fun MessagePart.extractText(): String {
    findPart("text/plain")?.let { return it.decode() }
    findPart("text/html")?.let {
        return it.decode().replace(Regex("(?s)<(style|script).*?</\\1>"), " ")
            .replace(Regex("<[^>]+>"), " ")
            .replace("&nbsp;", " ").replace("&amp;", "&")
            .replace(Regex("\\s+"), " ")
    }
    return ""
}

private fun MessagePart.findPart(mime: String): MessagePart? {
    if (mimeType.equals(mime, true) && !body?.data.isNullOrEmpty()) return this
    parts?.forEach { p -> p.findPart(mime)?.let { return it } }
    return null
}

private fun MessagePart.decode(): String = runCatching {
    String(Base64.decode(body?.data.orEmpty(), Base64.URL_SAFE), Charsets.UTF_8)
}.getOrDefault("")

private fun parseAmount(text: String): Pair<Double?, String?> {
    // 1) amount next to a "total" keyword, 2) otherwise the largest amount found
    TOTAL_RE.find(text)?.let { m -> toValue(m.groupValues[1], m.groupValues[2])?.let { return it } }
    return AMOUNT_RE.findAll(text)
        .mapNotNull { toValue(it.groupValues[1], it.groupValues[2]) }
        .maxByOrNull { it.first }
        ?: (null to null)
}

private fun toValue(symbol: String, raw: String): Pair<Double, String>? {
    val cur = when (symbol.uppercase().trimEnd('.')) {
        "RP", "IDR" -> "IDR"
        "$", "USD" -> "USD"
        "€", "EUR" -> "EUR"
        else -> symbol.uppercase()
    }
    // Indonesian: 150.000,50  | English: 1,234.56
    val normalized = if (cur == "IDR") raw.replace(".", "").replace(",", ".") else raw.replace(",", "")
    return normalized.toDoubleOrNull()?.let { it to cur }
}