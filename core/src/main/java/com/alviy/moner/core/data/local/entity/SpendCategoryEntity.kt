package com.alviy.moner.core.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expense_category",
    indices = [
        Index(value = ["categoryName","monthYear"], unique = true),
        Index("monthYear")
    ]
)
data class SpendCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val categoryId: Int = 0,
    val categoryName: String,
    val monthYear: String,
    val dateCreated: String
)
