package com.alviy.moner.api.gmail.parser.template

import com.alviy.moner.api.gmail.domain.model.PaymentMethod
import com.alviy.moner.api.gmail.domain.model.ReceiptTransaction
import com.alviy.moner.api.gmail.parser.text.AmountParser
import com.alviy.moner.api.gmail.parser.text.EmailText
import com.alviy.moner.api.gmail.parser.text.ReceiptDateParser

/**
 * Grab e-receipt: two real layouts, both one cell per row with the value on the row BELOW its label (no ":").
 *  - Ride (English): "Grab Bike", "Picked up on 13 October 2026", "Total Paid" / "Rp16.300", "Paid by" / "OVO",
 *    then the stops: pickup name, pickup time, drop-off name, drop-off time.
 *  - GrabFood (Indonesian): "Kamu membayar" / "Rp 29983", "TANGGAL | WAKTU" / "12 Oct 26 10:55 +0700",
 *    "Pesanan Dari:" / restaurant. For group orders "Kamu membayar" is the user's own share, not the order total.
 */
object GrabTemplate : ReceiptTemplate {

    private val STOP_TIME = Regex("""^\d{1,2}:\d{2}\s*[AaPp][Mm]$""")
    private const val STOP_SEPARATOR = "⋮"
    private const val STOP_ROWS = 4
    private const val SERVICE_NAME_MAX_LENGTH = 30

    private const val FOOD_MARKER = "Pesanan Dari"
    private const val TITLE_FOOD = "GrabFood"
    private const val TITLE_RIDE = "Grab ride"
    private const val SERVICE_PREFIX = "Grab"
    private const val PICKED_UP_PREFIX = "Picked up on"

    private class TripStops(val pickupTime: String, val dropoffName: String)

    override fun parse(ctx: MessageContext, channel: String): ReceiptTransaction? =
        if (ctx.text.below(FOOD_MARKER) != null) parseFood(ctx, channel) else parseRide(ctx, channel)

    private fun parseFood(ctx: MessageContext, channel: String): ReceiptTransaction? {
        val t = ctx.text
        val money = AmountParser.parse(t.below("Kamu membayar") ?: t.below("TOTAL")) ?: return null
        val vehicle = t.below("Jenis Kendaraan")                     // "Group Order GrabFood"
        return ctx.build(
            channel = channel,
            title = if (vehicle == null || TITLE_FOOD.lowercase() in vehicle.lowercase()) TITLE_FOOD else vehicle,
            destination = t.below(FOOD_MARKER) ?: channel,          // the restaurant
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
        val pickedUp = t.rows
            .firstOrNull { it.first().startsWith(PICKED_UP_PREFIX, ignoreCase = true) }
            ?.joinToString(" ")
        val service = t.rows.firstOrNull()?.firstOrNull()            // "Grab Bike"
            ?.takeIf { it.startsWith(SERVICE_PREFIX, ignoreCase = true) && it.length <= SERVICE_NAME_MAX_LENGTH }
        return ctx.build(
            channel = channel,
            title = service ?: TITLE_RIDE,
            destination = stops?.dropoffName ?: channel,
            money = money,
            parsedDate = ReceiptDateParser.parseDetailed(pickedUp, stops?.pickupTime),
            method = PaymentMethod.fromText(t.below("Paid by")),     // "OVO" -> E-wallet
            note = null,
        )
    }

    /** After the last "⋮" separator: pickup name, pickup time, drop-off name, drop-off time. */
    private fun tripStops(t: EmailText): TripStops? {
        val last = t.rows.indexOfLast { it.first().trim() == STOP_SEPARATOR }
        if (last < 0) return null
        val r = t.rows.drop(last + 1).map { it.joinToString(" ").trim() }
        if (r.size < STOP_ROWS || !STOP_TIME.matches(r[1]) || !STOP_TIME.matches(r[3])) return null
        return TripStops(pickupTime = r[1], dropoffName = r[2])
    }
}
