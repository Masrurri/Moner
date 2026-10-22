package com.alviy.moner.api.gmail.data.repository

import com.alviy.moner.api.gmail.data.remote.service.GmailRemoteDataSource
import com.alviy.moner.api.gmail.mapper.GmailReceiptParser
import com.alviy.moner.api.gmail.model.GmailMessage
import com.alviy.moner.api.gmail.model.GmailProfileResponse
import com.alviy.moner.core.common.AppPreferences
import com.alviy.moner.core.common.entity.ErrorCodes
import com.alviy.moner.core.common.entity.Result
import com.alviy.moner.core.common.entity.flatMap
import com.alviy.moner.core.common.entity.propagateError
import com.alviy.moner.core.common.ext.resultFlow
import com.alviy.moner.core.data.local.dao.ReceiptDao
import com.alviy.moner.core.data.mapper.toEntity
import com.alviy.moner.core.data.mapper.toReceipt
import com.alviy.moner.core.domain.model.Receipt
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import javax.inject.Inject

class GmailRepositoryImpl @Inject constructor(
    private val remote: GmailRemoteDataSource,
    private val parser: GmailReceiptParser,
    private val receiptDao: ReceiptDao,
    private val prefs: AppPreferences,
) : GmailRepository {

    override fun fetchNewReceipts(): Flow<Result<Int>> = resultFlow {
        // An empty table (first run, or after "clear all data") means a full first fetch again.
        val after = prefs.receiptsFetchedUntil.takeIf { it > 0 && receiptDao.count() > 0 }
        listMessageIds(parser.searchQuery(after)).flatMap { ids -> fetchAndSave(ids) }
    }

    /** Every page of the search; Gmail returns newest first. */
    private suspend fun listMessageIds(query: String): Result<List<String>> {
        val ids = mutableListOf<String>()
        var pageToken: String? = null
        do {
            val page = remote.listMessages(query, PAGE_SIZE, pageToken)
            if (!page.isSuccess) return page.propagateError()
            page.data?.messages?.mapTo(ids) { it.id }
            pageToken = page.data?.nextPageToken
        } while (pageToken != null && ids.size < MAX_MESSAGES)   // shortcut: stops at MAX_MESSAGES, raise it if a first fetch ever hits the cap
        return Result.success(ids)
    }

    private suspend fun fetchAndSave(ids: List<String>): Result<Int> = coroutineScope {
        val newIds = ids - receiptDao.getExistingIds(ids).toSet()   // already saved: don't download again
        val gate = Semaphore(MAX_PARALLEL)
        val results: List<Result<GmailMessage>> = newIds
            .map { id -> async { gate.withPermit { remote.getMessage(id) } } }
            .awaitAll()

        // Expired token: fail the whole load so the UI can ask the user to re-authorize.
        results.firstOrNull { it.code == ErrorCodes.UNAUTHORIZED }?.let {
            return@coroutineScope it.propagateError<Int>()
        }
        if (results.isNotEmpty() && results.all { it.isError }) {
            return@coroutineScope results.first().propagateError()   // every fetch failed
        }

        val messages = results.mapNotNull { it.data }
        val receipts = messages.mapNotNull(parser::parse)            // null = not a payment, reason logged
        receiptDao.insertReceipts(receipts.map(Receipt::toEntity))

        // Advance only when every message arrived, so one that failed to download is retried next time.
        if (messages.size == results.size) {
            messages.maxOfOrNull { it.internalDate?.toLongOrNull() ?: 0L }
                ?.let { newest -> prefs.receiptsFetchedUntil = maxOf(prefs.receiptsFetchedUntil, newest) }
        }
        Result.success(receipts.size)
    }

    override fun getReceipts(): Flow<List<Receipt>> =
        receiptDao.getReceipts().map { rows -> rows.map { it.toReceipt() } }

    override suspend fun deleteReceipt(id: String) = receiptDao.markDeleted(id)

    override fun getGmailProfileEmail(): Flow<Result<GmailProfileResponse>> =
        resultFlow { remote.getProfile() }

    private companion object {
        const val MAX_PARALLEL = 5
        const val PAGE_SIZE = 100
        const val MAX_MESSAGES = 800    // also keeps getExistingIds under SQLite's 999-argument limit
    }
}
