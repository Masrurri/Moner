package com.alviy.app.gmail.data.repository

import com.alviy.app.gmail.model.GmailReceiptTransaction
import com.alviy.moner.core.common.entity.Result
import kotlinx.coroutines.flow.Flow

interface GmailRepository {
    /** Emits LOADING, then SUCCESS(list) or ERROR. */
    fun getGmailReceipts(
        query: String = DEFAULT_QUERY,
        max: Int = 20,
    ): Flow<Result<List<GmailReceiptTransaction>>>

    companion object {
        const val DEFAULT_QUERY =
            "(subject:(receipt OR invoice OR struk OR pembayaran OR transaksi OR \"order confirmation\")) newer_than:60d"
    }
}