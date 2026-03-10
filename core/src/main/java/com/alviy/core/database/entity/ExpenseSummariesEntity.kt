package com.alviy.core.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ExpenseSummariesEntity(
    @Embedded val summary: ExpenseSummaryEntity,

    @Relation(
        parentColumn = "monthYear",
        entityColumn = "monthYear"
    )
    val categorySummaries: List<CategorySummariesEntity>
)