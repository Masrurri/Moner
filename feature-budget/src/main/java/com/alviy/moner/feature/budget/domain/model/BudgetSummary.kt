package com.alviy.moner.feature.budget.domain.model

import java.math.BigDecimal

data class BudgetSummary(
    val totalLimit: BigDecimal = BigDecimal.ZERO,
    val totalSpent: BigDecimal = BigDecimal.ZERO
) {
    val progressFraction: Float get() = progressFraction(totalSpent, totalLimit)
}
