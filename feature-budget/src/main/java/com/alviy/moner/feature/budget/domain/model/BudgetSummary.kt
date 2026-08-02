package com.alviy.moner.feature.budget.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

data class BudgetSummary(
    val totalLimit: BigDecimal = BigDecimal.ZERO,
    val totalSpent: BigDecimal = BigDecimal.ZERO
) {
    val progressFraction: Float
        get() = if (totalLimit <= BigDecimal.ZERO) 0f
        else (totalSpent.divide(totalLimit, 4, RoundingMode.HALF_UP).toFloat()).coerceIn(0f, 1f)
}
