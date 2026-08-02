package com.alviy.moner.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(
    tableName = "category_budget",
    indices = [
        Index(value = ["categoryName", "monthYear"], unique = true)
    ]
)
data class CategoryBudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val budgetId: Int = 0,
    val categoryName: String,
    val monthYear: String,
    val monthlyLimit: BigDecimal,
    val period: String,
    val notifyAt80Percent: Boolean,
    val dateCreated: String
)
