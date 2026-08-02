package com.alviy.moner.core.data.mapper

import com.alviy.moner.core.data.local.entity.ExpenseSummaryEntity
import com.alviy.moner.core.domain.model.ExpenseSummary

fun ExpenseSummaryEntity?.toExpenseSummary(): ExpenseSummary {
    return if (this == null) ExpenseSummary()
    else ExpenseSummary(
        monthYear = this.monthYear,
        totalExpense = this.totalExpense,
        latestExpense = this.latestExpense,
        latestTransactionDate = this.latestTransactionDate
    )
}
