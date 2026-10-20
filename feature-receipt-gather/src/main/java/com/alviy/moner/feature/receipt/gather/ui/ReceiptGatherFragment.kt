package com.alviy.moner.feature.receipt.gather.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.alviy.moner.core.common.base.BaseFragment
import com.alviy.moner.core.common.ext.showDialog
import com.alviy.moner.core.designsystem.TransactionListDialog
import com.alviy.moner.feature.receipt.gather.databinding.FragmentReceiptGatherBinding
import com.alviy.moner.feature.receipt.gather.ui.model.toReceiptListUi
import com.alviy.moner.feature.receipt.gather.viewmodel.ReceiptGatherViewModel
import com.google.android.gms.auth.api.identity.AuthorizationRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.common.api.Scope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.map

@AndroidEntryPoint
class ReceiptGatherFragment : BaseFragment<FragmentReceiptGatherBinding>() {

    private val viewModel: ReceiptGatherViewModel by viewModels()
    private val receiptListAdapter = ReceiptListAdapter()

    private val authLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            try {
                val auth = Identity.getAuthorizationClient(requireActivity())
                    .getAuthorizationResultFromIntent(result.data)
                auth.accessToken?.let(viewModel::onAuthGoogleSuccess) ?: viewModel.onAuthGoogleFailed("No access token")
            } catch (e: Exception) {
                viewModel.onAuthGoogleFailed(e.message)
            }
        }

    override val injectedFragment = this

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentReceiptGatherBinding.inflate(inflater, container, false)

    override fun initView() = with(binding) {
        rvReceipts.adapter = receiptListAdapter
        rvReceipts.layoutManager = LinearLayoutManager(requireContext())
        btnFetch.setOnClickListener { authorizeGoogleAccount() }
    }

    override fun observeData() {
        observeFetchedReceipts()
    }

    private fun observeFetchedReceipts() {
        observeStateFlow(
            flow = viewModel.fetchedReceipts,
            onLoading = { showReceipts(listOf(receiptMessage("Loading"))) },
            onError = { error ->
                showReceipts(
                    listOf(receiptMessage("Error Fetch", error.code.toString(), error.title.toString(), error.message.toString()))
                )
            }
        ) { receipts ->
            receiptListAdapter.submitList(receipts.toReceiptListUi())
//            showReceipts(
//                receipts.map { r ->
//                    TransactionListDialog.Companion.TransactionReceipt(
//                        r.id, r.channel, r.title, r.destination,
//                        r.amount.toPlainString(),
//                        listOfNotNull(r.date, r.time).joinToString(" "),
//                        r.method?.label.orEmpty(),
//                        r.note.orEmpty(),
//                    )
//                }
//            )
        }
    }

    private fun receiptMessage(id: String, channel: String = "", title: String = "", destination: String = "") =
        TransactionListDialog.Companion.TransactionReceipt(id, channel, title, destination, "", "", "", "")

    private fun showReceipts(receipts: List<TransactionListDialog.Companion.TransactionReceipt>) {
        TransactionListDialog.newInstance(receipts).showDialog(parentFragmentManager, "transactionDialog")
    }

    private fun authorizeGoogleAccount() {
        val request = AuthorizationRequest.builder()
            .setRequestedScopes(listOf(Scope(GMAIL_READONLY)))
            .build()
        Identity.getAuthorizationClient(requireActivity()).authorize(request)
            .addOnSuccessListener { result ->
                if (result.hasResolution()) {
                    authLauncher.launch(IntentSenderRequest.Builder(result.pendingIntent!!.intentSender).build())
                } else {
                    result.accessToken?.let(viewModel::onAuthGoogleSuccess)
                }
            }
            .addOnFailureListener { viewModel.onAuthGoogleFailed(it.message) }
    }

    private companion object {
        const val GMAIL_READONLY = "https://www.googleapis.com/auth/gmail.readonly"
    }
}