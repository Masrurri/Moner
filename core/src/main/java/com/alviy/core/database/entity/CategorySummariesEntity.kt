package com.alviy.core.database.entity

import androidx.room.Entity
import java.math.BigDecimal

@Entity(
    tableName = "category_summary",
    primaryKeys = ["monthYear", "categoryId"]
)
data class CategorySummariesEntity(
    val monthYear: String,
    val categoryId: Int,
    val categoryName: String,
    val totalExpense: BigDecimal,
    val latestTrxDate: String
)