package com.alviy.moner.feature.receipt.gather.viewmodel

import com.alviy.moner.api.gmail.data.repository.GmailRepository
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

    fun onAuthGoogleSuccess(accessToken: String) {
        googleAuthTokens.accessToken = accessToken
        fetchGmailReceipts()
    }

    fun onAuthGoogleFailed(message: String?) {
        _fetchedReceipts.value = Result.error(ErrorCodes.GENERAL, "Authorization failed", message ?: "Authorization failed")
    }

    fun fetchGmailReceipts() {
        gmailRepository.getGmailReceipts().collectFlow(_fetchedReceipts) { result ->
            if (result.code == ErrorCodes.UNAUTHORIZED) googleAuthTokens.accessToken = null // expired
        }
    }
}