package com.alviy.moner.core.domain.model

import java.math.BigDecimal

data class TransactionItem(
    val categoryId: Int = 0,
    val categoryName: String = "",
    val date: String = "",
    val desc: String = "",
    val source: String = "",
    val amount: BigDecimal = BigDecimal.ZERO
)
