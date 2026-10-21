package com.alviy.moner.api.gmail.mapper.parse

import javax.inject.Inject
import javax.inject.Singleton

data class ReceiptSource(
    val name: String,                             // channel shown in the app: Mandiri, BCA, Grab, OVO, Shopee
    val from: List<String>,                       // sender fragments: address, domain or display name
    val subjects: List<String> = emptyList(),     // subject phrases (empty = any subject from this sender)
    val template: ReceiptTemplate,
)

/**
 * EDIT THIS FILE. Each source feeds BOTH the Gmail search query and the routing of every fetched email to its
 * template, so a mismatch here means the email is fetched but skipped (or never fetched).
 *
 * To find the real sender/subject: set DISCOVERY_QUERY (e.g. "newer_than:30d from:bca"), run the app and read
 * Logcat tag "GmailReceiptParser". Emails matching no source are parsed with [GenericTemplate].
 */
object ReceiptSourceConfig {
    /** How far back the very first fetch looks; later fetches only ask for mail newer than the last one. */
    const val FIRST_FETCH_NEWER_THAN = "60d"

    /** null = normal mode (query built from [sources]). */
    val DISCOVERY_QUERY: String? = null

    /** Logs From, Subject, parsed rows and the result. Rows contain personal data: turn this off once setup is done. */
    const val DEBUG_LOG = true

    /** Channel for emails that match no source. */
    const val GENERIC_CHANNEL = "Others"

    val sources: List<ReceiptSource> = listOf(
        ReceiptSource(
            name = "BCA",
            from = listOf("bca.co.id"),                              // TODO verify the real sender
            subjects = listOf("Internet Transaction Journal"),
            template = BcaTemplate,
        ),
        ReceiptSource(
            name = "Mandiri",
            from = listOf("bankmandiri.co.id"),                      // TODO verify the real sender
            subjects = listOf("Pembayaran Berhasil", "Transfer Berhasil", "Top-up Berhasil"),
            template = LivinTemplate,
        ),
        ReceiptSource(
            name = "Grab",
            from = listOf("grab.com"),                               // TODO verify the real sender
            subjects = listOf("Grab E-Receipt"),
            template = GrabTemplate,
        ),
        // Add later, once you have a sample email from each:
        // ReceiptSource("OVO", from = listOf("ovo.id"), subjects = listOf("..."), template = GenericTemplate),
        // ReceiptSource("Shopee", from = listOf("shopee.co.id"), subjects = listOf("..."), template = GenericTemplate),
    )
}

@Singleton
class ReceiptSourceRegistry @Inject constructor() {
    private val sources = ReceiptSourceConfig.sources

    /** The discovery query if set, otherwise one built from the sources; [afterMillis] null = first fetch. */
    fun searchQuery(afterMillis: Long?): String = ReceiptSourceConfig.DISCOVERY_QUERY ?: buildQuery(afterMillis)

    /** (from:(a OR b) subject:("x" OR "y")) OR (...) after:1760000000  (Gmail takes epoch seconds) */
    fun buildQuery(afterMillis: Long?): String {
        val groups = sources.map { s ->
            listOfNotNull(
                s.from.takeIf { it.isNotEmpty() }?.joinToString(" OR ", "from:(", ")"),
                s.subjects.takeIf { it.isNotEmpty() }
                    ?.joinToString(" OR ", "subject:(", ")") { "\"${it.replace("\"", "")}\"" },
            ).joinToString(" ", "(", ")")
        }
        val window = afterMillis?.let { "after:${it / 1000}" } ?: "newer_than:${ReceiptSourceConfig.FIRST_FETCH_NEWER_THAN}"
        return "(${groups.joinToString(" OR ")}) $window"
    }

    fun match(from: String, subject: String): ReceiptSource? = sources.firstOrNull { s ->
        (s.from.isEmpty() || s.from.any { from.contains(it, ignoreCase = true) }) &&
                (s.subjects.isEmpty() || s.subjects.any { subject.contains(it, ignoreCase = true) })
    }
}
