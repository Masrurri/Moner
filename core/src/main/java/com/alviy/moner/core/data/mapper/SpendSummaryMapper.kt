package com.alviy.moner.core.data.mapper

import com.alviy.moner.core.data.local.entity.ExpenseSummaryEntity
import com.alviy.moner.core.domain.model.SpendSummary
import java.math.BigDecimal

fun ExpenseSummaryEntity?.toSpendSummary(
    totalSpendToday: BigDecimal,
    totalSpendWeek: BigDecimal
): SpendSummary = SpendSummary(
    monthYear = this?.monthYear,
    totalExpense = this?.totalExpense,
    totalSpendToday = totalSpendToday,
    totalSpendWeek = totalSpendWeek
)
