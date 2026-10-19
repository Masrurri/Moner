package com.alviy.moner.api.gmail.parser.template

import com.alviy.moner.api.gmail.domain.model.PaymentMethod
import com.alviy.moner.api.gmail.domain.model.ReceiptTransaction
import com.alviy.moner.api.gmail.parser.text.AmountParser
import com.alviy.moner.api.gmail.parser.text.ReceiptDateParser

/**
 * For senders without a dedicated template yet (OVO, Shopee...): label-based and forgiving.
 * Skips the email when it finds no total, so marketing mail from the same sender is ignored.
 */
class GenericTemplate(
    private val title: String,
    private val method: PaymentMethod? = null,
) : ReceiptTemplate {

    override fun parse(ctx: MessageContext, channel: String): ReceiptTransaction? {
        val t = ctx.text
        val money = AmountParser.parse(t.value(*AMOUNT_LABELS)) ?: return null
        return ctx.build(
            channel = channel,
            title = title,
            destination = t.value(*DESTINATION_LABELS) ?: channel,
            money = money,
            parsedDate = ReceiptDateParser.parseDetailed(t.value(*DATE_LABELS), t.value("Jam")),
            method = method ?: PaymentMethod.fromText(t.value(*METHOD_LABELS)),
            note = t.value(*NOTE_LABELS).meaningful(),
        )
    }

    private companion object {
        val AMOUNT_LABELS = arrayOf(
            "Total Pembayaran", "Total Bayar", "Total Belanja", "Total Transaksi",
            "Total Payment", "Total Paid", "Total", "Jumlah", "Nominal",
        )
        val DESTINATION_LABELS = arrayOf("Penerima", "Merchant", "Nama Toko", "Toko", "Pembayaran Ke", "Tujuan")
        val DATE_LABELS = arrayOf("Tanggal Transaksi", "Tanggal", "Waktu", "Date")
        val METHOD_LABELS = arrayOf("Metode Pembayaran", "Payment Method")
        val NOTE_LABELS = arrayOf("Keterangan", "Catatan", "Note")
    }
}
