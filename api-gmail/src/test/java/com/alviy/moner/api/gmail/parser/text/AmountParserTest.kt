package com.alviy.moner.api.gmail.parser.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class AmountParserTest {

    @Test
    fun `indonesian format with decimal comma`() {
        val money = AmountParser.parse("Rp 26.378,00")!!
        assertEquals(BigDecimal("26378"), money.value)
        assertEquals("IDR", money.currency)
    }

    @Test
    fun `english format with decimal point`() {
        val money = AmountParser.parse("IDR 4,000.00")!!
        assertEquals(BigDecimal("4000"), money.value)
        assertEquals("IDR", money.currency)
    }

    @Test
    fun `lone separator before three digits is a thousands mark`() {
        assertEquals(BigDecimal("16300"), AmountParser.parse("16.300")!!.value)
        assertNull(AmountParser.parse("16.300")!!.currency)
    }

    @Test
    fun `prefix without space`() {
        val money = AmountParser.parse("Rp16.300")!!
        assertEquals(BigDecimal("16300"), money.value)
        assertEquals("IDR", money.currency)
    }

    @Test
    fun `real decimals are kept`() {
        val money = AmountParser.parse("USD 3.50")!!
        assertEquals(BigDecimal("3.50"), money.value)
        assertEquals("USD", money.currency)
    }

    @Test
    fun `blank or numberless text is null`() {
        assertNull(AmountParser.parse(null))
        assertNull(AmountParser.parse("  "))
        assertNull(AmountParser.parse("no amount here"))
    }
}
