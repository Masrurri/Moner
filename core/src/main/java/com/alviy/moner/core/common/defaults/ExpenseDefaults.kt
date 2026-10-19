package com.alviy.moner.core.common.defaults

/**
 * Built-in values for expense input. These are stored in the database as-is,
 * so they are data (not UI copy) and must stay stable across languages.
 */
object ExpenseDefaults {

    /** Always offered as category pills, even on a fresh database with no transactions yet. */
    val CATEGORIES = listOf("Food", "Transport", "Shopping")

    val PAYMENT_SOURCES = listOf("Cash", "QRIS", "Debit")

    /** Defaults first, then whatever category names actually exist, without duplicates. */
    fun mergeWithCategories(fromDb: List<String>): List<String> = (CATEGORIES + fromDb).distinct()
}
