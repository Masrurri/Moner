package com.alviy.moner.core.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "profile")
data class ProfileEntity(
    @PrimaryKey
    val name: String
)
