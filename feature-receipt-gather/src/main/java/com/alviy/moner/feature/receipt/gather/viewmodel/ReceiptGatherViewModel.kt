package com.alviy.moner.feature.receipt.gather.viewmodel

import com.alviy.moner.api.gmail.data.repository.GmailRepository
import com.alviy.moner.api.gmail.model.ReceiptTransaction
import com.alviy.moner.core.common.base.BaseViewModel
import com.alviy.moner.core.common.entity.ErrorCodes
import com.alviy.moner.core.common.entity.Result
import com.alviy.moner.core.network.GoogleAuthTokenHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ReceiptGatherViewModel @Inject constructor(
    private val gmailRepository: GmailRepository,
    private val googleAuthTokens: GoogleAuthTokenHolder
) : BaseViewModel() {

    /** true = a Gmail token is held, so the button fetches; false = the button starts Gmail auth. */
    private val _isGmailAuthorized = MutableStateFlow(googleAuthTokens.accessToken != null)
    val isGmailAuthorized: StateFlow<Boolean> = _isGmailAuthorized.asStateFlow()

    private val _fetchedReceipts = MutableStateFlow<Result<List<ReceiptTransaction>>?>(null)
    val fetchedReceipts = _fetchedReceipts.asStateFlow()

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
        gmailRepository.getGmailReceipts().collectFlow(_fetchedReceipts) { result ->
            if (result.code == ErrorCodes.UNAUTHORIZED) onGmailNotAuthorized()
        }
    }
}
