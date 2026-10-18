package com.alviy.app.gmail.data.repository

import com.alviy.app.gmail.data.remote.service.GmailRemoteDataSource
import com.alviy.app.gmail.mapper.toReceipt
import com.alviy.app.gmail.model.GmailReceiptTransaction
import com.alviy.app.gmail.model.MessageRef
import com.alviy.moner.core.common.entity.ErrorCodes
import com.alviy.moner.core.common.entity.Result
import com.alviy.moner.core.common.entity.flatMap
import com.alviy.moner.core.common.entity.map
import com.alviy.moner.core.common.entity.propagateError
import com.alviy.moner.core.common.ext.resultFlow
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import javax.inject.Inject

class GmailRepositoryImpl @Inject constructor(
    private val remote: GmailRemoteDataSource,
) : GmailRepository {

    override fun getGmailReceipts(query: String, max: Int): Flow<Result<List<GmailReceiptTransaction>>> =
        resultFlow {
            remote.listMessages(query, max).flatMap { list ->
                fetchReceipts(list.messages.orEmpty())
            }
        }

    private suspend fun fetchReceipts(refs: List<MessageRef>): Result<List<GmailReceiptTransaction>> =
        coroutineScope {
            val gate = Semaphore(MAX_PARALLEL)
            val results = refs
                .map { ref -> async { gate.withPermit { remote.getMessage(ref.id).map { it.toReceipt() } } } }
                .awaitAll()

            // Expired token: fail the whole load so the UI can ask the user to re-authorize.
            results.firstOrNull { it.code == ErrorCodes.UNAUTHORIZED }?.let {
                return@coroutineScope it.propagateError<List<GmailReceiptTransaction>>()
            }

            val receipts = results.mapNotNull { it.data }
            if (receipts.isEmpty() && results.isNotEmpty()) {
                results.first().propagateError()          // every fetch failed -> surface the error
            } else {
                Result.success(receipts.sortedByDescending { it.dateMillis })  // partial failures are skipped
            }
        }

    private companion object {
        const val MAX_PARALLEL = 5
    }
}