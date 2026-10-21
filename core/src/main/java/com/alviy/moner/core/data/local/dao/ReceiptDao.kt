package com.alviy.moner.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alviy.moner.core.data.local.entity.ReceiptEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReceiptDao {
    /** IGNORE: a re-fetched email never resets isRecorded / isDeleted. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertReceipts(receipts: List<ReceiptEntity>)

    /** The subset of [ids] already stored, so their bodies are not downloaded again. */
    @Query("SELECT id FROM receipt WHERE id IN (:ids)")
    suspend fun getExistingIds(ids: List<String>): List<String>

    @Query("SELECT COUNT(*) FROM receipt")
    suspend fun count(): Int

    /** Receipts still to review or already recorded; deleted ones are hidden. Newest first. */
    @Query("SELECT * FROM receipt WHERE isDeleted = 0 ORDER BY transactionAt DESC")
    fun getReceipts(): Flow<List<ReceiptEntity>>

    // Soft delete: the row stays so the next fetch doesn't bring the email back.
    @Query("UPDATE receipt SET isDeleted = 1 WHERE id = :id")
    suspend fun markDeleted(id: String)

    @Query("UPDATE receipt SET isRecorded = 1 WHERE id = :id")
    suspend fun markRecorded(id: String)

    @Query("DELETE FROM receipt")
    suspend fun clearReceipts()
}
