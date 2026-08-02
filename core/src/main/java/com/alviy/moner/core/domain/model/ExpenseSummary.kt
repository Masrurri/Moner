package com.alviy.moner.core.domain.model

import java.math.BigDecimal

class ExpenseSummary(
    val monthYear: String? = null,
    val totalExpense: BigDecimal? = null,
    val latestExpense: BigDecimal? = null,
    val latestTransactionDate: String? = null
)