package com.alviy.moner.core.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import java.math.BigDecimal

@Entity(
    tableName = "category_summary",
    primaryKeys = ["monthYear", "categoryId"],
    indices = [
        Index("categoryId"),
        Index("monthYear")
    ]
)
data class CategorySummaryEntity(
    val monthYear: String,
    val categoryId: Int,
    @ColumnInfo(name = "totalExpense") val totalSpend: BigDecimal,
    val latestTrxDate: String
)