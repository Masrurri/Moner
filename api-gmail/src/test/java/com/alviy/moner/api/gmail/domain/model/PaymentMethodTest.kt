package com.alviy.moner.api.gmail.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PaymentMethodTest {

    @Test
    fun `keywords map to methods`() {
        assertEquals(PaymentMethod.QRIS, PaymentMethod.fromText("Pembayaran QRIS"))
        assertEquals(PaymentMethod.EWALLET, PaymentMethod.fromText("OVO"))
        assertEquals(PaymentMethod.TRANSFER, PaymentMethod.fromText("Top-up Berhasil"))
        assertEquals(PaymentMethod.CREDIT, PaymentMethod.fromText("Kartu Kredit"))
    }

    @Test
    fun `earlier groups win`() {
        assertEquals(PaymentMethod.DEBIT, PaymentMethod.fromText("debit transfer"))
    }

    @Test
    fun `unknown or null text`() {
        assertNull(PaymentMethod.fromText(null))
        assertNull(PaymentMethod.fromText("cash"))
    }
}
