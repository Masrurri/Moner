package com.alviy.moner.api.gmail.parser.template

import com.alviy.moner.api.gmail.domain.model.PaymentMethod
import com.alviy.moner.api.gmail.domain.model.ReceiptTransaction
import com.alviy.moner.api.gmail.parser.text.AmountParser
import com.alviy.moner.api.gmail.parser.text.EmailText
import com.alviy.moner.api.gmail.parser.text.ReceiptDateParser

/**
 * Livin' by Mandiri: the heading decides the kind (Transfer / Top-up / Pembayaran Berhasil).
 * Transfer and Top-up layouts come from screenshots; the Pembayaran layout is a best guess.
 */
object LivinTemplate : ReceiptTemplate {

    private val TOP_UP_MARKERS = listOf("top-up berhasil", "top up berhasil")
    private val TRANSFER_MARKERS = listOf("transfer berhasil")
    private val PAYMENT_MARKERS = listOf("pembayaran berhasil", "payment successful")
    private val QR_MARKERS = listOf("qris", "dengan qr")

    private val PARTY_LABELS = listOf("Penerima", "Penyedia Jasa", "Merchant", "Nama Merchant", "Tujuan Pembayaran")

    // "Total Transaksi" (incl. fee) wins over the base amount.
    private val AMOUNT_LABELS = arrayOf(
        "Total yang Dibayarkan", "Total Transaksi", "Total Pembayaran", "Jumlah Pembayaran",
        "Nominal Pembayaran", "Nominal Transaksi",
        "Jumlah Transfer", "Nominal Top-up", "Nominal Top up", "Nominal",
    )

    private const val TITLE_TOP_UP = "Top-up"
    private const val TITLE_TRANSFER = "Transfer"
    private const val TITLE_PAYMENT = "Pembayaran"
    private const val TITLE_PAYMENT_QRIS = "Pembayaran QRIS"
    private const val PARTY_MAX_LINES = 3

    /** Livin uses two layouts: "label | value" in one row, or the value on the row below the label. */
    private fun EmailText.field(vararg labels: String): String? =
        labels.firstNotNullOfOrNull { value(it) ?: below(it) }

    override fun parse(ctx: MessageContext, channel: String): ReceiptTransaction? {
        val t = ctx.text
        val haystack = (ctx.subject + " " + t.flat).lowercase()
        fun has(markers: List<String>) = markers.any { it in haystack }

        val (title, method) = when {
            has(TOP_UP_MARKERS) -> TITLE_TOP_UP to PaymentMethod.TRANSFER
            has(TRANSFER_MARKERS) -> TITLE_TRANSFER to PaymentMethod.TRANSFER
            has(PAYMENT_MARKERS) -> {
                // The QR payment email says "dengan QR" and has a "No. Ref. QRIS" row, but no "Jenis" label.
                val m = PaymentMethod.fromText(t.field("Jenis Transaksi", "Metode Pembayaran"))
                    ?: if (has(QR_MARKERS)) PaymentMethod.QRIS else null
                (if (m == PaymentMethod.QRIS) TITLE_PAYMENT_QRIS else TITLE_PAYMENT) to m
            }
            else -> return null                                      // not a payment email
        }

        val money = AmountParser.parse(t.field(*AMOUNT_LABELS)) ?: return null

        // Card layout: label, then name / number / bank on their own lines. Only the name is kept.
        val destination = PARTY_LABELS
            .firstNotNullOfOrNull { label -> t.lines(label, PARTY_MAX_LINES).firstOrNull() }
            ?: channel

        return ctx.build(
            channel = channel,
            title = title,
            destination = destination,
            money = money,
            parsedDate = ReceiptDateParser.parseDetailed(t.field("Tanggal"), t.field("Jam")),
            method = method,
            note = t.field("Keterangan").meaningful(),
        )
    }
}
