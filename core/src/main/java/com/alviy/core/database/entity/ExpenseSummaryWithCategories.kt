package com.alviy.core.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ExpenseSummaryWithCategories(
    @Embedded val summary: ExpenseSummaryEntity,

    @Relation(
        parentColumn = "monthYear",
        entityColumn = "monthYear"
    )
    val categorySummaries: List<CategorySummaryEntity>
)