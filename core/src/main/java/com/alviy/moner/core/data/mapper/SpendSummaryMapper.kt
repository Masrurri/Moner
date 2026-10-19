package com.alviy.moner.core.data.mapper

import com.alviy.moner.core.data.local.entity.SpendLimitEntity
import com.alviy.moner.core.data.local.entity.SpendSummaryEntity
import com.alviy.moner.core.domain.model.SpendSummary
import java.math.BigDecimal

fun SpendSummaryEntity?.toSpendSummary(
    monthYear: String,
    totalSpendToday: BigDecimal,
    totalSpendWeek: BigDecimal,
    limit: SpendLimitEntity?
): SpendSummary = SpendSummary(
    monthYear = monthYear,
    totalSpend = this?.totalSpend ?: BigDecimal.ZERO,
    totalSpendToday = totalSpendToday,
    totalSpendWeek = totalSpendWeek,
    spendLimit = limit?.amount
)
