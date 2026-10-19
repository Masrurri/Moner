package com.alviy.moner.api.gmail.parser.source

import com.alviy.moner.api.gmail.parser.template.BcaTemplate
import com.alviy.moner.api.gmail.parser.template.GrabTemplate
import com.alviy.moner.api.gmail.parser.template.LivinTemplate
import com.alviy.moner.api.gmail.parser.template.ReceiptTemplate

/** A sender whose receipt emails we can read. */
data class ReceiptSource(
    val name: String,                             // channel shown in the app: Mandiri, BCA, Grab, OVO, Shopee
    val from: List<String>,                       // sender fragments: address, domain or display name
    val subjects: List<String> = emptyList(),     // subject phrases (empty = any subject from this sender)
    val template: ReceiptTemplate,
)

/**
 * EDIT THIS FILE to add or tune senders. Each source feeds BOTH the Gmail search query and the routing of
 * every fetched email to its template, so a mismatch here means the email is fetched but skipped (or never fetched).
 *
 * To find the real sender/subject: set [ReceiptParserConfig.DISCOVERY_QUERY] (e.g. "newer_than:30d from:bca"),
 * run a debug build and read Logcat tag "GmailReceiptParser".
 */
object ReceiptSources {

    val ALL: List<ReceiptSource> = listOf(
        ReceiptSource(
            name = "BCA",
            from = listOf("bca.co.id"),                              // TODO verify the real sender
            subjects = listOf("Internet Transaction Journal"),
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
