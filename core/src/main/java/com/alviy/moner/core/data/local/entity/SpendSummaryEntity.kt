package com.alviy.moner.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import java.math.BigDecimal

/** Running spend total per month, updated on every new transaction. */
@Entity(
    tableName = "expense_summary",
    primaryKeys = ["monthYear"]
)
data class SpendSummaryEntity(
    val monthYear: String,
    @ColumnInfo(name = "totalExpense") val totalSpend: BigDecimal
)
