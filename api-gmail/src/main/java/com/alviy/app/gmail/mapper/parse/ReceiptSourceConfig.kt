package com.alviy.app.gmail.mapper.parse

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
 * Logcat tag "ReceiptExtractor".
 */
object ReceiptSourceConfig {
    const val NEWER_THAN = "60d"

    /** null = normal mode (query built from [sources]). */
    val DISCOVERY_QUERY: String? = null

    /** Logs From, Subject and parsed rows. Rows contain personal data: turn this off once setup is done. */
    const val DEBUG_LOG = true

    val sources: List<ReceiptSource> = listOf(
        ReceiptSource(
            name = "BCA",
            from = listOf("bca.co.id"),                              // TODO verify the real sender
            subjects = listOf("Internet Transaction Journal"),       // seen in the screenshot
            template = BcaTemplate,
        ),
        ReceiptSource(
            name = "Mandiri",
            from = listOf("bankmandiri.co.id"),                      // TODO verify the real sender
            subjects = listOf("Transfer Berhasil", "Top-up Berhasil", "Pembayaran Berhasil"),  // TODO verify real subjects
            template = LivinTemplate,
        ),
        ReceiptSource(
            name = "Grab",
            from = listOf("grab.com"),                               // TODO verify the real sender
            subjects = listOf("Your Grab E-Receipt"),                // TODO verify the real subject
            template = GrabTemplate,
        ),
        // Add later, once you have a sample email from each (GenericTemplate reads the common labels):
        // ReceiptSource("OVO", from = listOf("ovo.id"), subjects = listOf("..."), template = GenericTemplate("Pembayaran OVO", PaymentMethod.EWALLET)),
        // ReceiptSource("Shopee", from = listOf("shopee.co.id"), subjects = listOf("..."), template = GenericTemplate("Belanja Shopee")),
    )
}

@Singleton
class ReceiptSourceRegistry @Inject constructor() {
    private val sources = ReceiptSourceConfig.sources

    /** The discovery query if set, otherwise one built from the sources. */
    fun searchQuery(): String = ReceiptSourceConfig.DISCOVERY_QUERY ?: buildQuery()

    /** (from:(a OR b) subject:("x" OR "y")) OR (...) newer_than:60d */
    fun buildQuery(newerThan: String = ReceiptSourceConfig.NEWER_THAN): String {
        val groups = sources.map { s ->
            listOfNotNull(
                s.from.takeIf { it.isNotEmpty() }?.joinToString(" OR ", "from:(", ")"),
                s.subjects.takeIf { it.isNotEmpty() }
                    ?.joinToString(" OR ", "subject:(", ")") { "\"${it.replace("\"", "")}\"" },
            ).joinToString(" ", "(", ")")
        }
        return "(${groups.joinToString(" OR ")}) newer_than:$newerThan"
    }

    fun match(from: String, subject: String): ReceiptSource? = sources.firstOrNull { s ->
        (s.from.isEmpty() || s.from.any { from.contains(it, ignoreCase = true) }) &&
                (s.subjects.isEmpty() || s.subjects.any { subject.contains(it, ignoreCase = true) })
    }
}