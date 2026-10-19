package com.alviy.moner.api.gmail.parser.template

import com.alviy.moner.api.gmail.domain.model.PaymentMethod
import com.alviy.moner.api.gmail.domain.model.ReceiptTransaction
import com.alviy.moner.api.gmail.parser.text.AmountParser
import com.alviy.moner.api.gmail.parser.text.ReceiptDateParser

/** BCA "Internet Transaction Journal": "Label : value" rows, amount like "IDR 4,000.00". */
object BcaTemplate : ReceiptTemplate {

    private val SUCCESS_WORDS = listOf("berhasil", "success")
    private val AMOUNT_LABELS = arrayOf(
        "Total Bayar", "Nominal Tujuan", "Nominal Transfer", "Jumlah Transfer", "Jumlah", "Nominal",
    )
    private val DESTINATION_LABELS = arrayOf("Pembayaran Ke", "Nama Penerima", "Penerima", "Tujuan")
    private val DATE_LABELS = arrayOf("Tanggal Transaksi", "Tanggal")
    private val NOTE_LABELS = arrayOf("Keterangan", "Berita")

    private const val TITLE_TRANSFER = "Transfer"
    private const val TITLE_FALLBACK = "Transaksi BCA"

    override fun parse(ctx: MessageContext, channel: String): ReceiptTransaction? {
        val t = ctx.text
        val status = t.value("Status")
        if (status != null && SUCCESS_WORDS.none { status.contains(it, ignoreCase = true) }) return null

        val money = AmountParser.parse(t.value(*AMOUNT_LABELS)) ?: return null
        val jenis = t.value("Jenis Transaksi")                      // payments, e.g. "Pembayaran QRIS"
        val transferKind = t.value("Jenis Transfer")                // transfers; the text describes the schedule

        return ctx.build(
            channel = channel,
            title = jenis ?: if (transferKind != null) TITLE_TRANSFER else TITLE_FALLBACK,
            destination = t.value(*DESTINATION_LABELS) ?: channel,
            money = money,
            parsedDate = ReceiptDateParser.parseDetailed(t.value(*DATE_LABELS)),
            method = PaymentMethod.fromText(jenis ?: transferKind ?: ctx.subject),
            note = t.value(*NOTE_LABELS).meaningful(),
        )
    }
}
