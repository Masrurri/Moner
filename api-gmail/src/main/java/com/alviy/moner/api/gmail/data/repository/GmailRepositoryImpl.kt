package com.alviy.moner.api.gmail.data.repository

import com.alviy.moner.api.gmail.data.remote.service.GmailRemoteDataSource
import com.alviy.moner.api.gmail.mapper.GmailReceiptParser
import com.alviy.moner.api.gmail.model.MessageRef
import com.alviy.moner.api.gmail.model.ReceiptTransaction
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
    private val parser: GmailReceiptParser,
) : GmailRepository {

    override fun getGmailReceipts(query: String?, max: Int): Flow<Result<List<ReceiptTransaction>>> =
        resultFlow {
            remote.listMessages(query ?: parser.searchQuery(), max).flatMap { list ->
                fetchReceipts(list.messages.orEmpty())
            }
        }

    private suspend fun fetchReceipts(refs: List<MessageRef>): Result<List<ReceiptTransaction>> =
        coroutineScope {
            val gate = Semaphore(MAX_PARALLEL)
            val results: List<Result<ReceiptTransaction?>> = refs
                .map { ref ->
                    async {
                        // Only the network call holds a permit; parsing runs outside it.
                        val message = gate.withPermit { remote.getMessage(ref.id) }
                        message.map { parser.parse(it) }      // SUCCESS(null) = not a payment, skipped
                    }
                }
                .awaitAll()

            // Expired token: fail the whole load so the UI can ask the user to re-authorize.
            results.firstOrNull { it.code == ErrorCodes.UNAUTHORIZED }?.let {
                return@coroutineScope it.propagateError<List<ReceiptTransaction>>()
            }

            if (results.isNotEmpty() && results.all { it.isError }) {
                return@coroutineScope results.first().propagateError()    // every fetch failed
            }

            Result.success(results.mapNotNull { it.data })                // drops errors and non-payments
        }

    private companion object {
        const val MAX_PARALLEL = 5
    }
}