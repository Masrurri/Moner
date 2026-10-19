package com.alviy.moner.api.gmail.parser

import android.util.Base64
import android.util.Log
import com.alviy.moner.api.gmail.data.remote.dto.GmailMessage
import com.alviy.moner.api.gmail.data.remote.dto.MessagePart
import com.alviy.moner.api.gmail.domain.model.ReceiptTransaction
import com.alviy.moner.api.gmail.parser.source.ReceiptParserConfig
import com.alviy.moner.api.gmail.parser.source.ReceiptSourceRegistry
import com.alviy.moner.api.gmail.parser.template.MessageContext
import com.alviy.moner.api.gmail.parser.text.EmailText
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Regex/HTML only, no LLM. Entry point for the repository: build the Gmail query, then parse each message.
 *
 * Flow: GmailMessage -> (headers + decoded body) -> [ReceiptSourceRegistry.match] -> template -> ReceiptTransaction.
 */
@Singleton
class GmailReceiptParser @Inject constructor(
    private val registry: ReceiptSourceRegistry,
) {
    /** Gmail search query built from the configured receipt sources. */
    fun searchQuery(): String = registry.searchQuery()

    /** null = not a payment, unknown sender, or unparseable. Never throws. */
    fun parse(message: GmailMessage): ReceiptTransaction? {
        val from = message.header(HEADER_FROM).orEmpty()
        val subject = message.header(HEADER_SUBJECT).orEmpty()
        val text = message.emailText()

        if (ReceiptParserConfig.DEBUG_LOG) {
            Log.d(TAG, "from=[$from] subject=[$subject]\n" + text.rows.joinToString("\n") { it.joinToString(" | ") })
        }

        val source = registry.match(from, subject) ?: run {
            debugWarn("No source matches from=[$from] subject=[$subject]")
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
        if (parsed == null) debugWarn("${source.name}: could not parse \"$subject\" (${message.id})")
        return parsed
    }

    /** Sender and subject can identify the user, so they are only logged in debug builds. */
    private fun debugWarn(message: String) {
        if (ReceiptParserConfig.DEBUG_LOG) Log.w(TAG, message)
    }

    private fun GmailMessage.header(name: String): String? =
        payload?.headers?.firstOrNull { it.name.equals(name, ignoreCase = true) }?.value

    /** HTML is preferred because it keeps table rows/cells; falls back to the text/plain part. */
    private fun GmailMessage.emailText(): EmailText {
        val html = payload?.findPart(MIME_HTML)?.decode()
        if (!html.isNullOrBlank()) return EmailText.fromHtml(html)
        return EmailText.fromPlain(payload?.findPart(MIME_PLAIN)?.decode().orEmpty())
    }

    private fun MessagePart.findPart(mime: String): MessagePart? {
        if (mimeType.equals(mime, ignoreCase = true) && !body?.data.isNullOrEmpty()) return this
        parts?.forEach { part -> part.findPart(mime)?.let { return it } }
        return null
    }

    private fun MessagePart.decode(): String = runCatching {
        String(Base64.decode(body?.data.orEmpty(), Base64.URL_SAFE), Charsets.UTF_8)
    }.getOrDefault("")

    private companion object {
        const val TAG = "GmailReceiptParser"
        const val HEADER_FROM = "From"
        const val HEADER_SUBJECT = "Subject"
        const val MIME_HTML = "text/html"
        const val MIME_PLAIN = "text/plain"
    }
}
