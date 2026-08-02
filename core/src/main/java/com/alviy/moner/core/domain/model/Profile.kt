package com.alviy.moner.core.domain.model

import java.math.BigDecimal

data class Profile(
    val name: String = "",
    val monthYearExpense: String = "",
    val totalExpenses: BigDecimal = BigDecimal.ZERO,
    val latestExpense: BigDecimal = BigDecimal.ZERO
)
