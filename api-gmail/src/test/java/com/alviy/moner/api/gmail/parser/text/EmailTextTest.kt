package com.alviy.moner.api.gmail.parser.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class EmailTextTest {

    @Test
    fun `label and value in separate cells`() {
        val text = EmailText.fromHtml(
            "<table><tr><td>Total Bayar</td><td>: IDR 4,000.00</td></tr></table>"
        )
        assertEquals("IDR 4,000.00", text.value("Total Bayar"))
    }

    @Test
    fun `label and value in one cell`() {
        val text = EmailText.fromHtml("<p>Total Bayar : IDR 4,000.00</p>")
        assertEquals("IDR 4,000.00", text.value("total bayar"))
    }

    @Test
    fun `one cell per row with a lone colon row`() {
        val text = EmailText.fromPlain("Status\n:\nBerhasil")
        assertEquals("Berhasil", text.value("Status"))
    }

    @Test
    fun `labels are tried in order`() {
        val text = EmailText.fromPlain("Nominal  : 10.000\nTotal Bayar  : 12.000")
        assertEquals("12.000", text.value("Total Bayar", "Nominal"))
    }

    @Test
    fun `value below a label`() {
        val text = EmailText.fromPlain("Total Paid\nRp16.300")
        assertEquals("Rp16.300", text.below("Total Paid"))
        assertNull(text.below("Paid by"))
    }

    @Test
    fun `html entities and scripts are cleaned`() {
        val text = EmailText.fromHtml("<style>p{}</style><p>Merchant&nbsp;:&nbsp;Kopi &amp; Roti</p>")
        assertEquals("Kopi & Roti", text.value("Merchant"))
    }
}
