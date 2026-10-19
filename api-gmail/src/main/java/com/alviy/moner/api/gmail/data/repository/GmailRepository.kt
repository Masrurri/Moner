package com.alviy.moner.api.gmail.data.repository

import com.alviy.moner.api.gmail.model.ReceiptTransaction
import com.alviy.moner.core.common.entity.Result
import kotlinx.coroutines.flow.Flow

interface GmailRepository {
    /**
     * Emits LOADING, then SUCCESS(list) or ERROR.
     * [query] null = built from the senders and subjects in ReceiptSourceConfig.
     */
    fun getGmailReceipts(
        query: String? = null,
        max: Int = 50,
    ): Flow<Result<List<ReceiptTransaction>>>
}