package com.alviy.moner.feature.budget.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

private const val FRACTION_SCALE = 4

/** spent / limit clamped to 0f..1f; 0f when no limit is set. */
internal fun progressFraction(spent: BigDecimal, limit: BigDecimal): Float =
    if (limit <= BigDecimal.ZERO) 0f
    else spent.divide(limit, FRACTION_SCALE, RoundingMode.HALF_UP).toFloat().coerceIn(0f, 1f)
