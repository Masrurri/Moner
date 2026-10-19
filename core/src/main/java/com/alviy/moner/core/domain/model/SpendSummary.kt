package com.alviy.moner.core.domain.model

import java.math.BigDecimal

class SpendSummary(
    val monthYear: String? = null,
    val totalExpense: BigDecimal? = null,
    val totalSpendToday: BigDecimal = BigDecimal.ZERO,
    val totalSpendWeek: BigDecimal = BigDecimal.ZERO
)
