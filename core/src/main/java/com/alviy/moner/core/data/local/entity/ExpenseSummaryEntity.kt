package com.alviy.moner.core.data.local.entity

import androidx.room.Entity
import java.math.BigDecimal

@Entity(
    tableName = "expense_summary",
    primaryKeys = ["monthYear"]
)
data class ExpenseSummaryEntity(
    val monthYear: String,
    val totalExpense: BigDecimal
)
