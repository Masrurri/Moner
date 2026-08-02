package com.alviy.moner.core.domain.model

import java.math.BigDecimal
import java.time.YearMonth

data class ExpenseItem(
    val categoryId: Int = 0,
    val categoryName: String = "",
    val monthYear: String = "",
    val totalExpense: BigDecimal = BigDecimal.ZERO,
    val dateCreated: String = "",
    val transactions: List<TransactionItem>? = null
)
