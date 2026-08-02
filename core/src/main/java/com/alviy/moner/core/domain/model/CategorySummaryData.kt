package com.alviy.moner.core.domain.model

import java.math.BigDecimal

class CategorySummaryData(
    val monthYear: String? = null,
    val categoryId: Int? = null,
    val categoryName: String? = null,
    val totalExpense: BigDecimal? = null,
    val latestTrxDate: String? = null
)