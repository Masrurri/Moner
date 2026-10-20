package com.alviy.moner.feature.receipt.gather.viewmodel

import com.alviy.moner.api.gmail.data.repository.GmailRepository
import com.alviy.moner.api.gmail.model.GmailProfileResponse
import com.alviy.moner.api.gmail.model.ReceiptTransaction
import com.alviy.moner.core.common.base.BaseViewModel
import com.alviy.moner.core.common.entity.ErrorCodes
import com.alviy.moner.core.common.entity.Result
import com.alviy.moner.core.network.GoogleAuthTokenHolder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ReceiptGatherViewModel @Inject constructor(
    private val gmailRepository: GmailRepository,
    private val googleAuthTokens: GoogleAuthTokenHolder
) : BaseViewModel() {

    private val _fetchedReceipts = MutableStateFlow<Result<List<ReceiptTransaction>>?>(null)
    val fetchedReceipts = _fetchedReceipts.asStateFlow()

    /** Silent check found an existing grant. */
    fun onGoogleAlreadyAuthorized(accessToken: String) = connectWith(accessToken)

    /** Silent check found no grant. */
    fun onGoogleNotAuthorized() = markNotConnected()

    /** User finished the interactive connect flow. */
    fun onAuthGoogleSuccess(accessToken: String) = connectWith(accessToken)

    fun onAuthGoogleFailed() {
        googleAuthTokens.accessToken = null
    }

    private fun connectWith(accessToken: String) {
        googleAuthTokens.accessToken = accessToken
    }

    private fun markNotConnected() {
        googleAuthTokens.accessToken = null
    }

    // ---------------------------------------------------------------- Receipts

    /** Only offered once the profile loaded, i.e. a valid token is held. */
    fun fetchGmailReceipts() {
        gmailRepository.getGmailReceipts().collectFlow(_fetchedReceipts) { result ->
            if (result.code == ErrorCodes.UNAUTHORIZED) markNotConnected() // token expired: ask to connect again
        }
    }
}
