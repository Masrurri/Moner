package com.alviy.moner.core.data.mapper

import com.alviy.moner.core.data.local.entity.CategorySummaryEntity
import com.alviy.moner.core.domain.model.CategorySummaryData

fun CategorySummaryEntity?.toCategorySummaryData(): CategorySummaryData {
    return if (this == null) CategorySummaryData()
    else CategorySummaryData(
        monthYear = this.monthYear,
        categoryId = this.categoryId,
        totalExpense = this.totalExpense,
        latestTrxDate = this.latestTrxDate
    )
}
