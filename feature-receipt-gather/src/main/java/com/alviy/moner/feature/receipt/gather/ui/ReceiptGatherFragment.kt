package com.alviy.moner.feature.receipt.gather.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.alviy.moner.core.common.base.BaseFragment
import com.alviy.moner.core.common.ext.getColor
import com.alviy.moner.core.common.ext.setStatusBarColor
import com.alviy.moner.core.common.ext.showDialog
import com.alviy.moner.core.designsystem.TransactionListDialog
import com.alviy.moner.feature.receipt.gather.R
import com.alviy.moner.feature.receipt.gather.databinding.FragmentReceiptGatherBinding
import com.alviy.moner.feature.receipt.gather.ui.model.toReceiptListUi
import com.alviy.moner.feature.receipt.gather.viewmodel.ReceiptGatherViewModel
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import dagger.hilt.android.AndroidEntryPoint
import com.alviy.moner.core.R as CoreR

@AndroidEntryPoint
class ReceiptGatherFragment : BaseFragment<FragmentReceiptGatherBinding>() {

    private val viewModel: ReceiptGatherViewModel by viewModels()
    private val receiptListAdapter = ReceiptListAdapter()

    private val authLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            try {
                val auth = Identity.getAuthorizationClient(requireActivity())
                    .getAuthorizationResultFromIntent(result.data)
                auth.accessToken?.let(viewModel::onGmailAuthorized) ?: viewModel.onGmailNotAuthorized()
            } catch (e: Exception) {
                viewModel.onGmailNotAuthorized()
            }
        }

    override val injectedFragment = this

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentReceiptGatherBinding.inflate(inflater, container, false)

    override fun initView() = with(binding) {
        setStatusBarColor(binding.getColor(CoreR.color.white))
        rvReceipts.adapter = receiptListAdapter
        rvReceipts.layoutManager = LinearLayoutManager(requireContext())
        btnFetch.setOnClickListener {
            if (viewModel.isGmailAuthorized.value) viewModel.fetchGmailReceipts() else authorizeGmail()
        }
        checkGoogleAuthStatus()
    }

    override fun observeData() {
        observeState(viewModel.isGmailAuthorized, onChange = ::renderFetchButton)
        observeFetchedReceipts()
    }

    /** Not authorized yet → "Auth Gmail" with the Google icon; authorized → "Fetch Receipt". */
    private fun renderFetchButton(isAuthorized: Boolean) = with(binding.btnFetch) {
        if (isAuthorized) {
            setText(R.string.receipt_gather_fetch)
            setIconResource(CoreR.drawable.ic_fetch_square)
        } else {
            setText(R.string.receipt_gather_auth_gmail)
            setIconResource(CoreR.drawable.ic_google)
        }
    }

    private fun observeFetchedReceipts() {
        observeStateFlow(
            flow = viewModel.fetchedReceipts,
            onLoading = { renderReceiptsLoading(true) },
            onError = { error ->
                renderReceiptsLoading(false)
                showReceipts(
                    listOf(receiptMessage("Error Fetch", error.code.toString(), error.title.toString(), error.message.toString()))
                )
            }
        ) { receipts ->
            renderReceiptsLoading(false)
            receiptListAdapter.submitList(receipts.toReceiptListUi())
        }
    }

    private fun renderReceiptsLoading(isLoading: Boolean) = with(binding) {
        shimmerReceipts.isVisible = isLoading
        shimmerReceipts.setShimmering(isLoading)
        rvReceipts.isVisible = !isLoading
    }

    private fun receiptMessage(id: String, channel: String = "", title: String = "", destination: String = "") =
        TransactionListDialog.Companion.TransactionReceipt(id, channel, title, destination, "", "", "", "")

    private fun showReceipts(receipts: List<TransactionListDialog.Companion.TransactionReceipt>) {
        TransactionListDialog.newInstance(receipts).showDialog(parentFragmentManager, "transactionDialog")
    }

    // ---------------------------------------------------------------- Google auth

    private fun buildAuthRequest() = AuthorizationRequest.builder()
        .setRequestedScopes(listOf(Scope(GMAIL_READONLY)))
        .build()

    /** Silent check on open: never launches UI, only decides which label the button shows. */
    private fun checkGoogleAuthStatus() {
        Identity.getAuthorizationClient(requireActivity()).authorize(buildAuthRequest())
            .addOnSuccessListener { result ->
                val token = result.accessToken
                if (!result.hasResolution() && token != null) {
                    viewModel.onGmailAuthorized(token)
                } else {
                    viewModel.onGmailNotAuthorized()           // needs consent → show "Auth Gmail"
                }
            }
            .addOnFailureListener { viewModel.onGmailNotAuthorized() }
    }

    /** Interactive auth from the button; shows the consent screen when needed. */
    private fun authorizeGmail() {
        Identity.getAuthorizationClient(requireActivity()).authorize(buildAuthRequest())
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    authLauncher.launch(IntentSenderRequest.Builder(result.pendingIntent!!.intentSender).build())
                } else {
                    result.accessToken?.let(viewModel::onGmailAuthorized) ?: viewModel.onGmailNotAuthorized()
                }
            }
            .addOnFailureListener { viewModel.onGmailNotAuthorized() }
    }

    private companion object {
        const val GMAIL_READONLY = "https://www.googleapis.com/auth/gmail.readonly"
    }
}
