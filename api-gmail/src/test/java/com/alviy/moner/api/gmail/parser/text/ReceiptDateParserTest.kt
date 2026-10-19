package com.alviy.moner.api.gmail.parser.text

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

class ReceiptDateParserTest {

    private val jakarta = ZoneId.of("Asia/Jakarta")

    private fun millis(y: Int, m: Int, d: Int, h: Int = 0, min: Int = 0, s: Int = 0, zone: ZoneId = jakarta) =
        ZonedDateTime.of(y, m, d, h, min, s, 0, zone).toInstant().toEpochMilli()

    @Test
    fun `indonesian month with separate WIB time`() {
        val parsed = ReceiptDateParser.parseDetailed("14 Okt 2026", "10:07:03 WIB")!!
        assertEquals(millis(2026, 10, 14, 10, 7, 3), parsed.millis)
        assertTrue(parsed.hasTime)
    }

    @Test
    fun `two digit year with numeric offset`() {
        val parsed = ReceiptDateParser.parseDetailed("12 Oct 26 10:55 +0700")!!
        assertEquals(millis(2026, 10, 12, 10, 55, zone = ZoneOffset.ofHours(7)), parsed.millis)
    }

    @Test
    fun `WITA zone is respected`() {
        val parsed = ReceiptDateParser.parseDetailed("1 Januari 2026 08:00 WITA")!!
        assertEquals(millis(2026, 1, 1, 8, zone = ZoneId.of("Asia/Makassar")), parsed.millis)
    }

    @Test
    fun `pm time`() {
        val parsed = ReceiptDateParser.parseDetailed("13 October 2026", "9:41 PM")!!
        assertEquals(millis(2026, 10, 13, 21, 41), parsed.millis)
    }

    @Test
    fun `date without time is midnight WIB`() {
        val parsed = ReceiptDateParser.parseDetailed("Picked up on 13 October 2026")!!
        assertEquals(millis(2026, 10, 13), parsed.millis)
        assertFalse(parsed.hasTime)
    }

    @Test
    fun `numeric day first date`() {
        assertEquals(millis(2026, 3, 5), ReceiptDateParser.parse("05/03/2026"))
    }

    @Test
    fun `unparseable text is null`() {
        assertNull(ReceiptDateParser.parse("hello"))
        assertNull(ReceiptDateParser.parse(null))
    }

    @Test
    fun `display format is fixed to Jakarta time`() {
        val millis = millis(2026, 10, 14, 10, 7)
        assertEquals("14/10/2026", ReceiptDateFormat.date(millis))
        assertEquals("10:07", ReceiptDateFormat.time(millis))
    }
}
