package com.alviy.moner.api.gmail.mapper.parse

import com.alviy.moner.core.domain.model.PaymentMethod
import com.alviy.moner.core.domain.model.Receipt
import com.alviy.moner.core.domain.model.ReceiptActivity

class MessageContext(
    val id: String,                 // Gmail message id
    val from: String,
    val subject: String,
    val text: EmailText,
    val receivedAt: Long,           // Gmail's receive time; fills in a missing date or time
) {
    /** Why a field is missing or fell back; GmailReceiptParser logs these for debugging. */
    val notes = mutableListOf<String>()

    /** Records why the email was skipped and returns null, for `?: return ctx.skip("...")`. */
    fun skip(reason: String): Receipt? {
        notes += "skipped: $reason"
        return null
    }
}

/** One template per sender/layout. Return null (via [MessageContext.skip]) when the email is not a payment. */
fun interface ReceiptTemplate {
    fun parse(ctx: MessageContext, channel: String): Receipt?
}

private fun String?.meaningful(): String? = this?.trim()?.takeIf { it.isNotEmpty() && it != "-" && it != "–" }

private fun String.hasWord(vararg words: String): Boolean =
    words.any { Regex("""\b${Regex.escape(it)}\b""", RegexOption.IGNORE_CASE).containsMatchIn(this) }

/** Whole words only, so "dana" doesn't match "pendanaan" and "atm" doesn't match inside a name. */
internal fun paymentMethodOf(text: String?): PaymentMethod? {
    val s = text ?: return null
    return when {
        s.hasWord("qris", "qr") -> PaymentMethod.QRIS
        s.hasWord("debit") -> PaymentMethod.DEBIT
        s.hasWord("kredit", "credit", "kartu kredit", "credit card", "cc", "visa", "mastercard", "jcb") -> PaymentMethod.CREDIT_CARD
        s.hasWord("atm") -> PaymentMethod.ATM
        s.hasWord("ovo", "gopay", "dana", "shopeepay", "linkaja", "grabpay", "e-wallet", "ewallet", "wallet") -> PaymentMethod.EWALLET
        else -> null
    }
}

/** Top-up first: "Pembayaran Top Up GoPay" is a top-up, not a payment. */
internal fun activityOf(text: String?): ReceiptActivity? {
    val s = text ?: return null
    return when {
        s.hasWord("top-up", "top up", "topup") -> ReceiptActivity.TOP_UP
        s.hasWord("transfer") -> ReceiptActivity.TRANSFER
        s.hasWord("pembayaran", "payment", "bayar", "purchase") -> ReceiptActivity.PAYMENT
        else -> null
    }
}

/** Shared by every template so blank fields, date fallback and logging behave the same everywhere. */
private fun MessageContext.receipt(
    channel: String,
    activity: ReceiptActivity,
    destination: String?,
    reference: String?,
    money: Money,
    date: ParsedDate?,
    method: PaymentMethod?,
    note: String?,
): Receipt {
    when {
        date == null -> notes += "date+time: using received time"
        date.time == null -> notes += "time: using received time"
    }
    if (destination.meaningful() == null) notes += "destination: using channel"
    if (method == null) notes += "method: unknown"
    return Receipt(
        id = id,
        channel = channel,
        activity = activity,
        destination = destination.meaningful() ?: channel,
        reference = reference.meaningful(),
        amount = money.value,
        transactionAt = date?.toMillis(receivedAt) ?: receivedAt,
        receivedAt = receivedAt,
        method = method,
        note = note.meaningful(),
    )
}

/** BCA "Internet Transaction Journal": "Label : value" rows, amount like "IDR 4,000.00". */
object BcaTemplate : ReceiptTemplate {
    override fun parse(ctx: MessageContext, channel: String): Receipt? {
        val t = ctx.text
        val status = t.value("Status")
        if (status != null && !status.contains("berhasil", true) && !status.contains("success", true)) {
            return ctx.skip("status \"$status\"")
        }
        val money = AmountParser.parse(
            t.value("Total Bayar", "Nominal Tujuan", "Nominal Transfer", "Jumlah Transfer", "Jumlah", "Nominal")
        ) ?: return ctx.skip("no amount")
        val jenis = t.value("Jenis Transaksi")                      // payments, e.g. "Pembayaran QRIS"
        val transferKind = t.value("Jenis Transfer")                // transfers; the text describes the schedule

        return ctx.receipt(
            channel = channel,
            activity = activityOf(jenis) ?: if (transferKind != null) ReceiptActivity.TRANSFER else ReceiptActivity.PAYMENT,
            destination = t.value("Pembayaran Ke", "Nama Penerima", "Penerima", "Tujuan"),
            reference = t.value("No. Referensi", "Nomor Referensi", "No. Ref", "Rekening Tujuan", "No. Rekening Tujuan"),
            money = money,
            date = ReceiptDateParser.parse(t.value("Tanggal Transaksi", "Tanggal")),
            method = paymentMethodOf(jenis ?: transferKind),
            note = t.value("Keterangan", "Berita"),
        )
    }
}

/**
 * Livin' by Mandiri. The subject decides the activity: "Pembayaran Berhasil" / "Transfer Berhasil" / "Top-up Berhasil".
 * Transfer and Top-up layouts come from screenshots; the Pembayaran layout is a best guess.
 */
object LivinTemplate : ReceiptTemplate {
    private val PARTY_LABELS = arrayOf("Penerima", "Penyedia Jasa", "Merchant", "Nama Merchant", "Tujuan Pembayaran")

    /** Livin uses two layouts: "label | value" in one row, or the value on the row below the label. */
    private fun EmailText.field(vararg labels: String): String? =
        labels.firstNotNullOfOrNull { value(it) ?: below(it) }

    override fun parse(ctx: MessageContext, channel: String): Receipt? {
        val t = ctx.text
        val activity = activityOf(ctx.subject) ?: return ctx.skip("subject is not Pembayaran/Transfer/Top-up")

        // "Total Transaksi" (incl. fee) wins over the base amount.
        val money = AmountParser.parse(
            t.field(
                "Total yang Dibayarkan", "Total Transaksi", "Total Pembayaran", "Jumlah Pembayaran",
                "Nominal Pembayaran", "Nominal Transaksi",
                "Jumlah Transfer", "Nominal Top-up", "Nominal Top up", "Nominal",
            )
        ) ?: return ctx.skip("no amount")

        // Labelled fields first; the body is only scanned for QR, since ads in the footer mention cards too.
        // The QR payment email says "dengan QR" and has a "No. Ref. QRIS" row, but no "Jenis" label.
        val method = paymentMethodOf(t.field("Jenis Transaksi", "Metode Pembayaran", "Sumber Dana", "Jenis Kartu"))
            ?: PaymentMethod.QRIS.takeIf {
                activity == ReceiptActivity.PAYMENT && t.flat.hasWord("qris", "dengan qr")
            }

        // Card layout: label, then name / account number / bank on their own lines.
        val card = PARTY_LABELS.firstNotNullOfOrNull { label -> t.lines(label, 3).takeIf { it.isNotEmpty() } }

        return ctx.receipt(
            channel = channel,
            activity = activity,
            destination = card?.firstOrNull() ?: t.field(*PARTY_LABELS),
            reference = t.field("No. Referensi", "Nomor Referensi", "No. Ref. QRIS", "No. Ref", "ID Transaksi")
                ?: card?.getOrNull(1),
            money = money,
            date = ReceiptDateParser.parse(t.field("Tanggal"), t.field("Jam")),
            method = method,
            note = t.field("Keterangan", "Catatan"),
        )
    }
}

/**
 * Grab e-receipt: two real layouts, both one cell per row with the value on the row BELOW its label (no ":").
 *  - Ride (English): "Grab Bike", "Picked up on 13 October 2026", "Total Paid" / "Rp16.300", "Paid by" / "OVO",
 *    then the stops: pickup name, pickup time, drop-off name, drop-off time.
 *  - GrabFood (Indonesian): "Kamu membayar" / "Rp 29983", "TANGGAL | WAKTU" / "12 Oct 26 10:55 +0700",
 *    "Pesanan Dari:" / restaurant. For group orders "Kamu membayar" is the user's own share, not the order total.
 * Payment method defaults to E-Wallet when the email doesn't name one.
 */
object GrabTemplate : ReceiptTemplate {
    private val STOP_TIME = Regex("""^\d{1,2}:\d{2}\s*[AaPp][Mm]$""")
    private val REFERENCE_LABELS = arrayOf("Booking ID", "Kode Booking", "ID Pesanan", "Order ID")

    private class TripStops(val pickupTime: String, val dropoffName: String)

    override fun parse(ctx: MessageContext, channel: String): Receipt? =
        if (ctx.text.below("Pesanan Dari") != null) parseFood(ctx, channel) else parseRide(ctx, channel)

    private fun parseFood(ctx: MessageContext, channel: String): Receipt? {
        val t = ctx.text
        val money = AmountParser.parse(t.below("Kamu membayar") ?: t.below("TOTAL")) ?: return ctx.skip("no amount")
        return ctx.receipt(
            channel = channel,
            activity = ReceiptActivity.GRAB_FOOD,
            destination = t.below("Pesanan Dari"),                   // the restaurant
            reference = t.below(*REFERENCE_LABELS),
            money = money,
            date = ReceiptDateParser.parse(t.below("Tanggal")),
            method = paymentMethodOf(t.below("Metode Pembayaran", "Dibayar dengan")) ?: PaymentMethod.EWALLET,
            note = null,
        )
    }

    private fun parseRide(ctx: MessageContext, channel: String): Receipt? {
        val t = ctx.text
        val money = AmountParser.parse(t.below("Total Paid", "Total Dibayar")) ?: return ctx.skip("no amount")
        val stops = tripStops(t)
        val pickedUp = t.rows.firstOrNull { it.first().startsWith("Picked up on", ignoreCase = true) }?.joinToString(" ")
        // "Grab Bike" / "GrabCar Plus": compare without spaces.
        val service = t.rows.firstOrNull()?.firstOrNull()?.lowercase()?.replace(" ", "").orEmpty()
        val activity = when {
            "bikestandard" in service -> ReceiptActivity.GRAB_BIKE
            "grabbike" in service -> ReceiptActivity.GRAB_BIKE
            "grabcar" in service -> ReceiptActivity.GRAB_CAR
            else -> ReceiptActivity.OTHER.also { ctx.notes += "activity: unknown Grab service \"$service\"" }
        }
        return ctx.receipt(
            channel = channel,
            activity = activity,
            destination = stops?.dropoffName,
            reference = t.below(*REFERENCE_LABELS),
            money = money,
            date = ReceiptDateParser.parse(pickedUp, stops?.pickupTime),
            method = paymentMethodOf(t.below("Paid by")) ?: PaymentMethod.EWALLET,   // "OVO" -> E-Wallet
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
 * For senders without a dedicated template yet: label-based and forgiving.
 * Skips the email when it finds no total, so marketing mail from the same sender is ignored.
 */
object GenericTemplate : ReceiptTemplate {
    override fun parse(ctx: MessageContext, channel: String): Receipt? {
        val t = ctx.text
        val money = AmountParser.parse(
            t.value(
                "Total Pembayaran", "Total Bayar", "Total Belanja", "Total Transaksi",
                "Total Payment", "Total Paid", "Total", "Jumlah", "Nominal",
            )
        ) ?: return ctx.skip("no amount")
        return ctx.receipt(
            channel = channel,
            activity = activityOf(t.value("Jenis Transaksi") ?: ctx.subject) ?: ReceiptActivity.PAYMENT,
            destination = t.value("Penerima", "Merchant", "Nama Toko", "Toko", "Pembayaran Ke", "Tujuan"),
            reference = t.value("No. Referensi", "Nomor Referensi", "No. Ref", "Order ID", "No. Pesanan", "ID Transaksi"),
            money = money,
            date = ReceiptDateParser.parse(t.value("Tanggal Transaksi", "Tanggal", "Waktu", "Date"), t.value("Jam")),
            method = paymentMethodOf(t.value("Metode Pembayaran", "Payment Method")),
            note = t.value("Keterangan", "Catatan", "Note"),
        )
    }
}
