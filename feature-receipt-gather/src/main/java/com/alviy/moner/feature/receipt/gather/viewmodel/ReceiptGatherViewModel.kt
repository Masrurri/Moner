package com.alviy.moner.feature.receipt.gather.viewmodel

import androidx.lifecycle.viewModelScope
import com.alviy.moner.api.gmail.data.repository.GmailRepository
import com.alviy.moner.core.common.base.BaseViewModel
import com.alviy.moner.core.common.entity.ErrorCodes
import com.alviy.moner.core.common.entity.Result
import com.alviy.moner.core.data.mapper.toNewSpend
import com.alviy.moner.core.designsystem.SpendCategory
import com.alviy.moner.core.domain.model.Receipt
import com.alviy.moner.core.domain.repository.SpendRepository
import com.alviy.moner.core.network.GoogleAuthTokenHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReceiptGatherViewModel @Inject constructor(
    private val gmailRepository: GmailRepository,
    private val googleAuthTokens: GoogleAuthTokenHolder,
    private val spendRepository: SpendRepository
) : BaseViewModel() {

    /** true = a Gmail token is held, so the button fetches; false = the button starts Gmail auth. */
    private val _isGmailAuthorized = MutableStateFlow(googleAuthTokens.accessToken != null)
    val isGmailAuthorized: StateFlow<Boolean> = _isGmailAuthorized.asStateFlow()

    /** Saved receipts (from the DB), so the list survives restarts and fills in as a fetch saves. */
    /** null = first DB load hasn't emitted yet (shimmer); a list = loaded (may be empty). */
    private val _receipts = MutableStateFlow<List<Receipt>?>(null)
    val receipts: StateFlow<List<Receipt>?> = _receipts.asStateFlow()

    /** Progress of the last fetch: SUCCESS holds the number of new receipts saved. */
    private val _fetchResult = MutableStateFlow<Result<Int>?>(null)
    val fetchResult: StateFlow<Result<Int>?> = _fetchResult.asStateFlow()

    init {
        viewModelScope.launch {
            gmailRepository.getReceipts().collect { _receipts.value = it }
        }
    }

    // ---------------------------------------------------------------- Gmail auth

    /** Silent check on open, or the interactive auth flow, returned a token. */
    fun onGmailAuthorized(accessToken: String) {
        googleAuthTokens.accessToken = accessToken
        _isGmailAuthorized.value = true
    }

    /** No grant yet, the user cancelled, or the token expired. */
    fun onGmailNotAuthorized() {
        googleAuthTokens.accessToken = null
        _isGmailAuthorized.value = false
    }

    // ---------------------------------------------------------------- Receipts

    /** Only offered while authorized; an expired token flips the button back to "Auth Gmail". */
    fun fetchGmailReceipts() {
        gmailRepository.fetchNewReceipts().collectFlow(_fetchResult) { result ->
            if (result.code == ErrorCodes.UNAUTHORIZED) onGmailNotAuthorized()
        }
    }

    fun recordReceipt(receiptId: String, category: SpendCategory) {
        val receipt = _receipts.value?.firstOrNull { it.id == receiptId } ?: return
        if (receipt.isRecorded) return
        val spend = receipt.toNewSpend(category)
        viewModelScope.launch {
            spendRepository.addSpend(
                categoryName = spend.categoryName,
                date = spend.date,
                desc = spend.desc,
                source = spend.source,
                amount = spend.amount,
                note = spend.note,
                receiptId = spend.receiptId,
            )
        }
    }

    fun deleteReceipt(id: String) = Unit
}
