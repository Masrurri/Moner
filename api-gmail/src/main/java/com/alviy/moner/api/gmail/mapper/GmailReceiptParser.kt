package com.alviy.moner.api.gmail.mapper

import android.util.Base64
import android.util.Log
import com.alviy.moner.api.gmail.mapper.parse.EmailText
import com.alviy.moner.api.gmail.mapper.parse.GenericTemplate
import com.alviy.moner.api.gmail.mapper.parse.MessageContext
import com.alviy.moner.api.gmail.mapper.parse.ReceiptSourceConfig
import com.alviy.moner.api.gmail.mapper.parse.ReceiptSourceRegistry
import com.alviy.moner.api.gmail.model.GmailMessage
import com.alviy.moner.api.gmail.model.MessagePart
import com.alviy.moner.core.domain.model.Receipt
import javax.inject.Inject
import javax.inject.Singleton

/** Regex/HTML only, no LLM. Entry point for the repository: build the Gmail query, then parse each message. */
@Singleton
class GmailReceiptParser @Inject constructor(
    private val registry: ReceiptSourceRegistry,
) {
    /** Gmail search query for mail received after [afterMillis] (null = first fetch). */
    fun searchQuery(afterMillis: Long?): String = registry.searchQuery(afterMillis)

    /** null = not a payment or unparseable; the reason is logged. Never throws. */
    fun parse(message: GmailMessage): Receipt? {
        val from = message.header("From").orEmpty()
        val subject = message.header("Subject").orEmpty()
        val text = message.emailText()
        val source = registry.match(from, subject)
        val channel = source?.name ?: ReceiptSourceConfig.GENERIC_CHANNEL
        val ctx = MessageContext(
            id = message.id,
            from = from,
            subject = subject,
            text = text,
            receivedAt = message.internalDate?.toLongOrNull() ?: System.currentTimeMillis(),
        )
        if (source == null) ctx.notes += "no source matches, using generic template"

        val receipt = try {
            (source?.template ?: GenericTemplate).parse(ctx, channel)
        } catch (e: Exception) {
            Log.w(TAG, "$channel template crashed on ${message.id}", e)
            null
        }

        if (ReceiptSourceConfig.DEBUG_LOG) {
            Log.d(TAG, "from=[$from] subject=[$subject] (${message.id})\n" +
                text.rows.joinToString("\n") { it.joinToString(" | ") })
            Log.d(TAG, "parsed=$receipt notes=${ctx.notes}")
        }
        if (receipt == null) Log.w(TAG, "$channel: no receipt from \"$subject\" (${message.id}): ${ctx.notes}")
        return receipt
    }

    private fun GmailMessage.header(name: String): String? =
        payload?.headers?.firstOrNull { it.name.equals(name, ignoreCase = true) }?.value

    /** HTML is preferred because it keeps table rows/cells; falls back to the text/plain part. */
    private fun GmailMessage.emailText(): EmailText {
        val html = payload?.findPart("text/html")?.decode()
        if (!html.isNullOrBlank()) return EmailText.fromHtml(html)
        return EmailText.fromPlain(payload?.findPart("text/plain")?.decode().orEmpty())
    }

    private fun MessagePart.findPart(mime: String): MessagePart? {
        if (mimeType.equals(mime, true) && !body?.data.isNullOrEmpty()) return this
        parts?.forEach { p -> p.findPart(mime)?.let { return it } }
        return null
    }

    private fun MessagePart.decode(): String = runCatching {
        String(Base64.decode(body?.data.orEmpty(), Base64.URL_SAFE), Charsets.UTF_8)
    }.getOrDefault("")

    private companion object { const val TAG = "GmailReceiptParser" }
}
