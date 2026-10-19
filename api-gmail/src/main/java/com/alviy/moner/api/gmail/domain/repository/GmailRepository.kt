package com.alviy.moner.api.gmail.domain.repository

import com.alviy.moner.api.gmail.domain.model.ReceiptTransaction
import com.alviy.moner.core.common.result.Result
import kotlinx.coroutines.flow.Flow

interface GmailRepository {
    /**
     * Emits LOADING, then SUCCESS(list) or ERROR.
     * [query] null = built from the configured receipt sources.
     */
    fun getGmailReceipts(
        query: String? = null,
        max: Int = DEFAULT_MAX_RESULTS,
    ): Flow<Result<List<ReceiptTransaction>>>

    companion object {
        const val DEFAULT_MAX_RESULTS = 50
    }
}
