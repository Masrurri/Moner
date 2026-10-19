package com.alviy.moner.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alviy.moner.core.data.local.entity.SpendLimitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpendLimitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertSpendLimit(limit: SpendLimitEntity)

    @Query("SELECT * FROM spend_limit WHERE monthYear = :monthYear LIMIT 1")
    fun getSpendLimit(monthYear: String): Flow<SpendLimitEntity?>

    @Query("DELETE FROM spend_limit")
    suspend fun clearSpendLimits()
}
