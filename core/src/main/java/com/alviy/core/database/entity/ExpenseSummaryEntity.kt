package com.alviy.core.database.entity

import androidx.room.Entity
import java.math.BigDecimal

@Entity(
    tableName = "expense_summary",
    primaryKeys = ["monthYear"]
)
data class ExpenseSummaryEntity(
    val monthYear: String,
    val totalExpense: BigDecimal,
    val latestExpense: BigDecimal,
    val latestTransactionDate: String
)