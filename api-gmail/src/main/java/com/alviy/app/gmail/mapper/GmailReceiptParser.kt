package com.alviy.app.gmail.mapper

import android.util.Base64
import android.util.Log
import com.alviy.app.gmail.mapper.parse.EmailText
import com.alviy.app.gmail.mapper.parse.MessageContext
import com.alviy.app.gmail.mapper.parse.ReceiptSourceConfig
import com.alviy.app.gmail.mapper.parse.ReceiptSourceRegistry
import com.alviy.app.gmail.model.GmailMessage
import com.alviy.app.gmail.model.MessagePart
import com.alviy.app.gmail.model.ReceiptTransaction
import javax.inject.Inject
import javax.inject.Singleton

/** Regex/HTML only, no LLM. Entry point for the repository: build the Gmail query, then parse each message. */
@Singleton
class GmailReceiptParser @Inject constructor(
    private val registry: ReceiptSourceRegistry,
) {
    /** Gmail search query built from the senders/subjects configured in ReceiptSourceConfig. */
    fun searchQuery(): String = registry.searchQuery()

    /** null = not a payment, unknown sender, or unparseable. Never throws. */
    fun parse(message: GmailMessage): ReceiptTransaction? {
        val from = message.header("From").orEmpty()
        val subject = message.header("Subject").orEmpty()
        val text = message.emailText()

        if (ReceiptSourceConfig.DEBUG_LOG) {
            Log.d(TAG, "from=[$from] subject=[$subject]\n" + text.rows.joinToString("\n") { it.joinToString(" | ") })
        }

        val source = registry.match(from, subject) ?: run {
            Log.w(TAG, "No source matches from=[$from] subject=[$subject]")
            return null
        }
        val ctx = MessageContext(
            id = message.id,
            from = from,
            subject = subject,
            text = text,
            fallbackMillis = message.internalDate?.toLongOrNull() ?: 0L,
        )
        val parsed = try {
            source.template.parse(ctx, source.name)
        } catch (e: Exception) {
            Log.w(TAG, "${source.name} template crashed on ${message.id}", e)
            null
        }
        if (parsed == null) Log.w(TAG, "${source.name}: could not parse \"$subject\" (${message.id})")
        return parsed
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