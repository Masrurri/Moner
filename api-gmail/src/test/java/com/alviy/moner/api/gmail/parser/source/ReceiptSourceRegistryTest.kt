package com.alviy.moner.api.gmail.parser.source

import com.alviy.moner.api.gmail.parser.template.ReceiptTemplate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReceiptSourceRegistryTest {

    private val noop = ReceiptTemplate { _, _ -> null }

    private val registry = ReceiptSourceRegistry(
        listOf(
            ReceiptSource("BCA", from = listOf("bca.co.id"), subjects = listOf("Internet Transaction Journal"), template = noop),
            ReceiptSource("Grab", from = listOf("grab.com", "Grab"), template = noop),
        )
    )

    @Test
    fun `query groups sender and subject per source`() {
        assertEquals(
            "((from:(bca.co.id) subject:(\"Internet Transaction Journal\")) OR (from:(grab.com OR Grab))) newer_than:7d",
            registry.buildQuery(newerThan = "7d"),
        )
    }

    @Test
    fun `match needs both sender and subject`() {
        assertEquals("BCA", registry.match("BCA <noreply@bca.co.id>", "Internet Transaction Journal")?.name)
        assertNull(registry.match("noreply@bca.co.id", "Promo"))
    }

    @Test
    fun `source without subjects matches any subject`() {
        assertEquals("Grab", registry.match("Grab <no-reply@grab.com>", "Your trip")?.name)
    }

    @Test
    fun `unknown sender has no source`() {
        assertNull(registry.match("someone@example.com", "Internet Transaction Journal"))
    }
}
