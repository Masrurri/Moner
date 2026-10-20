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
    private val _googleProfile = MutableStateFlow<Result<GmailProfileResponse>?>(null)
    val googleProfile = _googleProfile.asStateFlow()

    private val _fetchedReceipts = MutableStateFlow<Result<List<ReceiptTransaction>>?>(null)
    val fetchedReceipts = _fetchedReceipts.asStateFlow()

    /** Silent check found an existing grant: just load the email. */
    fun onGoogleAlreadyAuthorized(accessToken: String) {
        googleAuthTokens.accessToken = accessToken
        fetchGoogleProfile()
    }

    /** Silent check found no grant. */
    fun onGoogleNotAuthorized() {
        googleAuthTokens.accessToken = null
        _googleProfile.value = Result.error(ErrorCodes.UNAUTHORIZED, "Not connected", "Google account not connected")
    }

    /** User completed the auth flow (button click). */
    fun onAuthGoogleSuccess(accessToken: String) {
        googleAuthTokens.accessToken = accessToken
        fetchGoogleProfile()
        fetchGmailReceipts()
    }

    fun onAuthGoogleFailed(message: String?) {
        _fetchedReceipts.value = Result.error(ErrorCodes.GENERAL, "Authorization failed", message ?: "Authorization failed")
    }

    private fun fetchGoogleProfile() {
        gmailRepository.getGmailProfileEmail().collectFlow(_googleProfile) { result ->
            if (result.code == ErrorCodes.UNAUTHORIZED) googleAuthTokens.accessToken = null
        }
    }

    fun fetchGmailReceipts() {
        gmailRepository.getGmailReceipts().collectFlow(_fetchedReceipts) { result ->
            if (result.code == ErrorCodes.UNAUTHORIZED) googleAuthTokens.accessToken = null
        }
    }
}