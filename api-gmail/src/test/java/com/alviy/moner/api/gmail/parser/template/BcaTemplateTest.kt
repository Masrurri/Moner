package com.alviy.moner.api.gmail.parser.template

import com.alviy.moner.api.gmail.domain.model.PaymentMethod
import com.alviy.moner.api.gmail.parser.text.EmailText
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class BcaTemplateTest {

    private fun context(rows: List<Pair<String, String>>) = MessageContext(
        id = "msg-1",
        from = "BCA <noreply@bca.co.id>",
        subject = "Internet Transaction Journal",
        text = EmailText.fromHtml(
            rows.joinToString("", "<table>", "</table>") { (label, value) -> "<tr><td>$label</td><td>: $value</td></tr>" }
        ),
        fallbackMillis = 0L,
    )

    @Test
    fun `successful QRIS payment`() {
        val receipt = BcaTemplate.parse(
            context(
                listOf(
                    "Status" to "Berhasil",
                    "Jenis Transaksi" to "Pembayaran QRIS",
                    "Pembayaran Ke" to "KOPI KENANGAN",
                    "Total Bayar" to "IDR 26,378.00",
                    "Tanggal Transaksi" to "14 Okt 2026 10:07:03 WIB",
                    "Keterangan" to "-",
                )
            ),
            channel = "BCA",
        )!!

        assertEquals("Pembayaran QRIS", receipt.title)
        assertEquals("KOPI KENANGAN", receipt.destination)
        assertEquals(BigDecimal("26378"), receipt.amount)
        assertEquals("14/10/2026", receipt.date)
        assertEquals("10:07", receipt.time)
        assertEquals(PaymentMethod.QRIS, receipt.method)
        assertNull(receipt.note)
    }

    @Test
    fun `failed transaction is skipped`() {
        val receipt = BcaTemplate.parse(
            context(listOf("Status" to "Gagal", "Total Bayar" to "IDR 1,000.00")),
            channel = "BCA",
        )
        assertNull(receipt)
    }
}
