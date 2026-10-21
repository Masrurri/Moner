package com.alviy.moner.api.gmail.data.repository

import com.alviy.moner.api.gmail.model.GmailProfileResponse
import com.alviy.moner.core.common.entity.Result
import com.alviy.moner.core.domain.model.Receipt
import kotlinx.coroutines.flow.Flow

interface GmailRepository {
    /**
     * Fetches receipts received since the last fetch (the last 60 days the first time) and saves them.
     * Emits LOADING, then SUCCESS(number of new receipts saved) or ERROR.
     */
    fun fetchNewReceipts(): Flow<Result<Int>>

    /** Saved receipts that are not deleted, newest first. Updates live as fetches and reviews land. */
    fun getReceipts(): Flow<List<Receipt>>

    /** Soft delete: hidden from the list, and never brought back by a later fetch. */
    suspend fun deleteReceipt(id: String)

    fun getGmailProfileEmail(): Flow<Result<GmailProfileResponse>>
}
