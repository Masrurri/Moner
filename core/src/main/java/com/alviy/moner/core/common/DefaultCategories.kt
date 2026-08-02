package com.alviy.moner.core.common

/**
 * Baseline categories always offered as pill options, even on a fresh
 * database with no transactions yet. Merged with whatever category names
 * actually exist in the DAO so the picker always matches real data.
 */
val DEFAULT_CATEGORIES = listOf("Food", "Transport", "Shopping")

fun mergeWithDefaultCategories(fromDb: List<String>): List<String> =
    (DEFAULT_CATEGORIES + fromDb).distinct()
