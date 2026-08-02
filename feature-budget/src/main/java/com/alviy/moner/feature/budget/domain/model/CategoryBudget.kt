package com.alviy.moner.feature.budget.domain.model

import java.math.BigDecimal

data class CategoryBudget(
    val categoryName: String,
    val monthlyLimit: BigDecimal,
    val period: BudgetPeriod,
    val notifyAt80Percent: Boolean
)
