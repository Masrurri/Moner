package com.alviy.moner.core.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

/** Spend overview for one month; today/week totals are always relative to the current date. */
data class SpendSummary(
    val monthYear: String = "",
    val totalSpend: BigDecimal = BigDecimal.ZERO,
    val totalSpendToday: BigDecimal = BigDecimal.ZERO,
    val totalSpendWeek: BigDecimal = BigDecimal.ZERO,
    /** null = no limit set for this month. */
    val spendLimit: BigDecimal? = null
) {
    /** Limit minus spend; negative once the limit is exceeded. null when no limit is set. */
    val remainingLimit: BigDecimal?
        get() = spendLimit?.subtract(totalSpend)

    /** Share of the limit already spent, clamped to 0..1. */
    val limitProgress: Float
        get() {
            val limit = spendLimit?.takeIf { it > BigDecimal.ZERO } ?: return 0f
            return totalSpend.divide(limit, 4, RoundingMode.HALF_UP).toFloat().coerceIn(0f, 1f)
        }
}
