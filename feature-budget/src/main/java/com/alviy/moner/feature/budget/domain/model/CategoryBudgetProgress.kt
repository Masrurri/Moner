package com.alviy.moner.feature.budget.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

data class CategoryBudgetProgress(
    val categoryName: String,
    val limit: BigDecimal,
    val spent: BigDecimal,
    val period: BudgetPeriod,
    val notifyAt80Percent: Boolean
) {
    val isOverBudget: Boolean get() = limit > BigDecimal.ZERO && spent > limit

    val overageAmount: BigDecimal get() = (spent - limit).coerceAtLeast(BigDecimal.ZERO)

    val progressFraction: Float
        get() = if (limit <= BigDecimal.ZERO) 0f
        else (spent.divide(limit, 4, RoundingMode.HALF_UP).toFloat()).coerceIn(0f, 1f)
}
