package com.alviy.moner.api.gmail.mapper.parse

import com.alviy.moner.core.domain.model.PaymentMethod
import com.alviy.moner.core.domain.model.ReceiptActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.ZonedDateTime

class ReceiptTemplatesTest {

    private fun wib(text: String) = ZonedDateTime.parse("$text+07:00[Asia/Jakarta]").toInstant().toEpochMilli()

    private fun ctx(subject: String, receivedAt: Long, vararg rows: List<String>) =
        MessageContext("msg-1", "noreply@bankmandiri.co.id", subject, EmailText(rows.toList()), receivedAt)

    @Test
    fun livinTopUp_readsCardAndTakesTimeFromReceivedAt() {
        val ctx = ctx(
            "Top-up Berhasil", wib("2026-10-14T10:07:00"),
            listOf("Top-up Berhasil"),
            listOf("Nominal Top-up", "Rp 100.000,00"),
            listOf("Tanggal", "14 Okt 2026"),
            listOf("Penerima"), listOf("GoPay Budi"), listOf("0812345"),
            listOf("No. Referensi", "REF123"),
        )
        val r = LivinTemplate.parse(ctx, "Mandiri")!!
        assertEquals(ReceiptActivity.TOP_UP, r.activity)
        assertEquals(BigDecimal("100000"), r.amount)
        assertEquals("GoPay Budi", r.destination)
        assertEquals("REF123", r.reference)
        assertEquals(wib("2026-10-14T10:07:00"), r.transactionAt)     // date from email, time from receive time
        assertTrue(ctx.notes.contains("time: using received time"))
    }

    @Test
    fun livinWithoutAmount_isSkippedWithReason() {
        val ctx = ctx("Transfer Berhasil", 0L, listOf("Transfer Berhasil"))
        assertNull(LivinTemplate.parse(ctx, "Mandiri"))
        assertTrue(ctx.notes.contains("skipped: no amount"))
    }

    @Test
    fun grabRide_mapsBikeAndDefaultsToEwallet() {
        val ctx = ctx(
            "Your Grab E-Receipt", 0L,
            listOf("Grab Bike"), listOf("Picked up on 13 October 2026"),
            listOf("Total Paid"), listOf("Rp16.300"),
            listOf("⋮"), listOf("Home"), listOf("9:05 AM"), listOf("Office"), listOf("9:30 AM"),
        )
        val r = GrabTemplate.parse(ctx, "Grab")!!
        assertEquals(ReceiptActivity.GRAB_BIKE, r.activity)
        assertEquals(PaymentMethod.EWALLET, r.method)
        assertEquals(BigDecimal("16300"), r.amount)
        assertEquals("Office", r.destination)
        assertEquals(wib("2026-10-13T09:05:00"), r.transactionAt)
    }

    @Test
    fun paymentMethod_matchesWholeWordsOnly() {
        assertEquals(PaymentMethod.DEBIT, paymentMethodOf("Kartu Debit"))
        assertEquals(PaymentMethod.ATM, paymentMethodOf("Tarik Tunai ATM"))
        assertEquals(PaymentMethod.QRIS, paymentMethodOf("Pembayaran QRIS"))
        assertNull(paymentMethodOf("Pendanaan"))
    }
}
