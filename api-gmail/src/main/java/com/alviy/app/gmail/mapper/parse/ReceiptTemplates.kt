package com.alviy.app.gmail.mapper.parse

import com.alviy.app.gmail.model.PaymentMethod
import com.alviy.app.gmail.model.ReceiptTransaction

class MessageContext(
    val id: String,                 // Gmail message id
    val from: String,
    val subject: String,
    val text: EmailText,
    val fallbackMillis: Long,       // Gmail's own receive time; used when the email has no parseable date
)

/** One template per sender/layout. Return null when the email is not a payment (it is then skipped). */
fun interface ReceiptTemplate {
    fun parse(ctx: MessageContext, channel: String): ReceiptTransaction?
}

private fun String?.meaningful(): String? = this?.trim()?.takeIf { it.isNotEmpty() && it != "-" && it != "–" }

/** The email's own date when parsed, otherwise Gmail's receive time. */
private fun MessageContext.whenMillis(parsed: ParsedDate?): Long = parsed?.millis ?: fallbackMillis

private fun MessageContext.build(
    channel: String,
    title: String,
    destination: String,
    money: Money,
    parsedDate: ParsedDate?,
    method: PaymentMethod?,
    note: String?,
) = ReceiptTransaction(
    id = id,
    channel = channel,
    title = title,
    destination = destination,
    amount = money.value,
    date = ReceiptDateFormat.date(whenMillis(parsedDate)),
    time = if (parsedDate == null || parsedDate.hasTime) ReceiptDateFormat.time(whenMillis(parsedDate)) else null,
    method = method,
    note = note,
)

/** BCA "Internet Transaction Journal": "Label : value" rows, amount like "IDR 4,000.00". */
object BcaTemplate : ReceiptTemplate {
    override fun parse(ctx: MessageContext, channel: String): ReceiptTransaction? {
        val t = ctx.text
        val status = t.value("Status")
        if (status != null && !status.contains("berhasil", true) && !status.contains("success", true)) return null

        val money = AmountParser.parse(
            t.value("Total Bayar", "Nominal Tujuan", "Nominal Transfer", "Jumlah Transfer", "Jumlah", "Nominal")
        ) ?: return null
        val jenis = t.value("Jenis Transaksi")                      // payments, e.g. "Pembayaran QRIS"
        val transferKind = t.value("Jenis Transfer")                // transfers; the text describes the schedule

        return ctx.build(
            channel = channel,
            title = jenis ?: if (transferKind != null) "Transfer" else "Transaksi BCA",
            destination = t.value("Pembayaran Ke", "Nama Penerima", "Penerima", "Tujuan") ?: channel,
            money = money,
            parsedDate = ReceiptDateParser.parseDetailed(t.value("Tanggal Transaksi", "Tanggal")),
            method = PaymentMethod.fromText(jenis ?: transferKind ?: ctx.subject),
            note = t.value("Keterangan", "Berita").meaningful(),
        )
    }
}

/**
 * Livin' by Mandiri: the heading decides the kind (Transfer / Top-up / Pembayaran Berhasil).
 * Transfer and Top-up layouts come from screenshots; the Pembayaran layout is a best guess.
 */
object LivinTemplate : ReceiptTemplate {
    private val PARTY_LABELS = listOf("Penerima", "Penyedia Jasa", "Merchant", "Nama Merchant", "Tujuan Pembayaran")

    /** Livin uses two layouts: "label | value" in one row, or the value on the row below the label. */
    private fun EmailText.field(vararg labels: String): String? =
        labels.firstNotNullOfOrNull { value(it) ?: below(it) }

    override fun parse(ctx: MessageContext, channel: String): ReceiptTransaction? {
        val t = ctx.text
        val haystack = (ctx.subject + " " + t.flat).lowercase()
        val (title, method) = when {
            "top-up berhasil" in haystack || "top up berhasil" in haystack -> "Top-up" to PaymentMethod.TRANSFER
            "transfer berhasil" in haystack -> "Transfer" to PaymentMethod.TRANSFER
            "pembayaran berhasil" in haystack || "payment successful" in haystack -> {
                // The QR payment email says "dengan QR" and has an "No. Ref. QRIS" row, but no "Jenis" label.
                val isQr = "qris" in haystack || "dengan qr" in haystack
                val m = PaymentMethod.fromText(t.field("Jenis Transaksi", "Metode Pembayaran"))
                    ?: if (isQr) PaymentMethod.QRIS else null
                (if (m == PaymentMethod.QRIS) "Pembayaran QRIS" else "Pembayaran") to m
            }
            else -> return null                                      // not a payment email
        }

        // "Total Transaksi" (incl. fee) wins over the base amount.
        val money = AmountParser.parse(
            t.field(
                "Total yang Dibayarkan", "Total Transaksi", "Total Pembayaran", "Jumlah Pembayaran",
                "Nominal Pembayaran", "Nominal Transaksi",
                "Jumlah Transfer", "Nominal Top-up", "Nominal Top up", "Nominal",
            )
        ) ?: return null

        // Card layout: label, then name / number / bank on their own lines. Only the name is kept.
        val destination = PARTY_LABELS
            .firstNotNullOfOrNull { label -> t.lines(label, 3).firstOrNull() }
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

/**
 * Grab e-receipt: two real layouts, both one cell per row with the value on the row BELOW its label (no ":").
 *  - Ride (English): "Grab Bike", "Picked up on 13 October 2026", "Total Paid" / "Rp16.300", "Paid by" / "OVO",
 *    then the stops: pickup name, pickup time, drop-off name, drop-off time.
 *  - GrabFood (Indonesian): "Kamu membayar" / "Rp 29983", "TANGGAL | WAKTU" / "12 Oct 26 10:55 +0700",
 *    "Pesanan Dari:" / restaurant. For group orders "Kamu membayar" is the user's own share, not the order total.
 */
object GrabTemplate : ReceiptTemplate {
    private val STOP_TIME = Regex("""^\d{1,2}:\d{2}\s*[AaPp][Mm]$""")

    private class TripStops(val pickupTime: String, val dropoffName: String)

    override fun parse(ctx: MessageContext, channel: String): ReceiptTransaction? =
        if (ctx.text.below("Pesanan Dari") != null) parseFood(ctx, channel) else parseRide(ctx, channel)

    private fun parseFood(ctx: MessageContext, channel: String): ReceiptTransaction? {
        val t = ctx.text
        val money = AmountParser.parse(t.below("Kamu membayar") ?: t.below("TOTAL")) ?: return null
        val vehicle = t.below("Jenis Kendaraan")                     // "Group Order GrabFood"
        return ctx.build(
            channel = channel,
            title = if (vehicle == null || "grabfood" in vehicle.lowercase()) "GrabFood" else vehicle,
            destination = t.below("Pesanan Dari") ?: channel,        // the restaurant
            money = money,
            parsedDate = ReceiptDateParser.parseDetailed(t.below("Tanggal")),
            method = null,                                           // the email doesn't say how it was paid
            note = null,
        )
    }

    private fun parseRide(ctx: MessageContext, channel: String): ReceiptTransaction? {
        val t = ctx.text
        val money = AmountParser.parse(t.below("Total Paid", "Total Dibayar")) ?: return null
        val stops = tripStops(t)
        val pickedUp = t.rows.firstOrNull { it.first().startsWith("Picked up on", ignoreCase = true) }?.joinToString(" ")
        val service = t.rows.firstOrNull()?.firstOrNull()
            ?.takeIf { it.startsWith("Grab", ignoreCase = true) && it.length <= 30 }      // "Grab Bike"
        return ctx.build(
            channel = channel,
            title = service ?: "Grab ride",
            destination = stops?.dropoffName ?: channel,
            money = money,
            parsedDate = ReceiptDateParser.parseDetailed(pickedUp, stops?.pickupTime),
            method = PaymentMethod.fromText(t.below("Paid by")),     // "OVO" -> E-wallet
            note = null,
        )
    }

    /** After the last "⋮" separator: pickup name, pickup time, drop-off name, drop-off time. */
    private fun tripStops(t: EmailText): TripStops? {
        val last = t.rows.indexOfLast { it.first().trim() == "⋮" }
        if (last < 0) return null
        val r = t.rows.drop(last + 1).map { it.joinToString(" ").trim() }
        if (r.size < 4 || !STOP_TIME.matches(r[1]) || !STOP_TIME.matches(r[3])) return null
        return TripStops(pickupTime = r[1], dropoffName = r[2])
    }
}

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
        val money = AmountParser.parse(
            t.value(
                "Total Pembayaran", "Total Bayar", "Total Belanja", "Total Transaksi",
                "Total Payment", "Total Paid", "Total", "Jumlah", "Nominal",
            )
        ) ?: return null
        return ctx.build(
            channel = channel,
            title = title,
            destination = t.value("Penerima", "Merchant", "Nama Toko", "Toko", "Pembayaran Ke", "Tujuan") ?: channel,
            money = money,
            parsedDate = ReceiptDateParser.parseDetailed(t.value("Tanggal Transaksi", "Tanggal", "Waktu", "Date"), t.value("Jam")),
            method = method ?: PaymentMethod.fromText(t.value("Metode Pembayaran", "Payment Method")),
            note = t.value("Keterangan", "Catatan", "Note").meaningful(),
        )
    }
}