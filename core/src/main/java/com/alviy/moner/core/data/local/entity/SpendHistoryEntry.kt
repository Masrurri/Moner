package com.alviy.moner.core.data.local.entity

import java.math.BigDecimal

/** One transaction joined with its category name; result row of TransactionDao.getSpendHistory. */
data class SpendHistoryEntry(
    val transactionId: Int,
    val categoryId: Int,
    val categoryName: String,
    val date: String,
    val desc: String,
    val source: String,
    val amount: BigDecimal,
    val note: String?
)
