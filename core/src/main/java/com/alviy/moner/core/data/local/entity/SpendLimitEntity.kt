package com.alviy.moner.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

/** User-set spending limit for one month. */
@Entity(tableName = "spend_limit")
data class SpendLimitEntity(
    @PrimaryKey val monthYear: String,
    val amount: BigDecimal
)
