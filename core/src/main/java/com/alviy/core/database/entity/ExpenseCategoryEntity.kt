package com.alviy.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(
    tableName = "expense_category",
    indices = [
        Index(value = ["categoryName","monthYear"], unique = true),
        Index("monthYear")
    ]
)
data class ExpenseCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val categoryId: Int = 0,
    val categoryName: String,
    val monthYear: String,
    val dateCreated: String
)
