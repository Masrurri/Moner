package com.alviy.moner.api.gmail.data.repository

import com.alviy.moner.api.gmail.data.remote.datasource.GmailRemoteDataSource
import com.alviy.moner.api.gmail.data.remote.dto.MessageRef
import com.alviy.moner.api.gmail.domain.model.ReceiptTransaction
import com.alviy.moner.api.gmail.domain.repository.GmailRepository
import com.alviy.moner.api.gmail.parser.GmailReceiptParser
import com.alviy.moner.core.common.ext.resultFlow
import com.alviy.moner.core.common.result.ErrorCodes
import com.alviy.moner.core.common.result.Result
import com.alviy.moner.core.common.result.flatMap
import com.alviy.moner.core.common.result.map
import com.alviy.moner.core.common.result.propagateError
import com.alviy.moner.core.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
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
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : GmailRepository {

    override fun getGmailReceipts(query: String?, max: Int): Flow<Result<List<ReceiptTransaction>>> =
        resultFlow(ioDispatcher) {
            remote.listMessages(query ?: parser.searchQuery(), max).flatMap { list ->
                fetchReceipts(list.messages.orEmpty())
            }
        }

    /** Fetches every message (at most [MAX_PARALLEL] at once) and keeps the ones that parse as payments. */
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
