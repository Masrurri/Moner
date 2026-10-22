package com.alviy.moner.core.designsystem

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResult
import com.alviy.moner.core.R
import com.alviy.moner.core.common.ext.bindCategory
import com.alviy.moner.core.common.ext.toIDR
import com.alviy.moner.core.data.mapper.suggestSpendCategory
import com.alviy.moner.core.databinding.ItemCategoryChipBinding
import com.alviy.moner.core.databinding.ItemReceiptDetailRowBinding
import com.alviy.moner.core.databinding.SheetReceiptDetailBinding
import com.alviy.moner.core.designsystem.ReceiptDetailSheet.Companion.REQUEST_KEY
import com.alviy.moner.core.designsystem.ReceiptDetailSheet.Companion.parseResult
import com.alviy.moner.core.domain.model.PaymentMethod
import com.alviy.moner.core.domain.model.Receipt
import com.alviy.moner.core.domain.model.ReceiptActivity
import com.google.android.material.chip.Chip
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Shows one parsed receipt with a suggested category the user can change (exactly one).
 * Swiping records it as a spend in that category; the button deletes it.
 * The choice goes back to the host as a fragment result (see [REQUEST_KEY] / [parseResult]),
 * so show it with the host's childFragmentManager and listen on that same manager.
 */
class ReceiptDetailSheet : FullScreenSheetDialogFragment() {

    enum class Action { RECORD, DELETE }

    /** What the user chose. [category] is the one selected when they swiped (also sent on delete, unused there). */
    data class Result(val receiptId: String, val action: Action, val category: SpendCategory)

    private lateinit var binding: SheetReceiptDetailBinding

    private val receipt: ReceiptArgs by lazy {
        requireNotNull(BundleCompat.getParcelable(requireArguments(), ARG_RECEIPT, ReceiptArgs::class.java))
    }

    private lateinit var selectedCategory: SpendCategory

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SheetReceiptDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Keep the user's pick across rotation; otherwise start from the suggestion.
        selectedCategory = savedInstanceState?.getString(STATE_CATEGORY)
            ?.let { runCatching { SpendCategory.valueOf(it) }.getOrNull() }
            ?: suggestSpendCategory(receipt.activity, receipt.destination)

        renderReceipt(receipt)
        renderCategories()

        binding.swipeRecord.text = getString(R.string.receipt_detail_swipe_record)
        binding.swipeRecord.onConfirmed = { sendResult(Action.RECORD) }
        binding.btnDelete.setOnClickListener { sendResult(Action.DELETE) }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (::selectedCategory.isInitialized) outState.putString(STATE_CATEGORY, selectedCategory.name)
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    private fun renderReceipt(r: ReceiptArgs) = with(binding) {
        tvChannel.text = getString(R.string.receipt_detail_channel_activity, r.channel, r.activity.label)
        tvAmount.text = r.amount.toIDR()
        tvDestination.text = r.destination

        llDetails.removeAllViews()
        addRow(R.string.receipt_detail_reference, r.reference)
        addRow(R.string.receipt_detail_method, r.method?.label)
        addRow(R.string.receipt_detail_transaction_at, formatDate(r.transactionAt))
        addRow(R.string.receipt_detail_received_at, formatDate(r.receivedAt))
        addRow(R.string.receipt_detail_note, r.note)

        // Already recorded → nothing to categorize or swipe, only delete stays available.
        cgCategory.isVisible = !r.isRecorded
        swipeRecord.isVisible = !r.isRecorded
        tvRecorded.isVisible = r.isRecorded
    }

    /** One pill per [SpendCategory]; the group allows exactly one checked pill. */
    private fun renderCategories() = with(binding.cgCategory) {
        setOnCheckedStateChangeListener(null)
        removeAllViews()
        val inflater = LayoutInflater.from(context)
        SpendCategory.entries.forEach { category ->
            val chip = ItemCategoryChipBinding.inflate(inflater, this, false).root.apply {
                id = View.generateViewId()
                bindCategory(category)
            }
            addView(chip)
            if (category == selectedCategory) check(chip.id)
        }
        setOnCheckedStateChangeListener { group, checkedIds ->
            val chip = checkedIds.firstOrNull()?.let { group.findViewById<Chip>(it) } ?: return@setOnCheckedStateChangeListener
            selectedCategory = chip.tag as SpendCategory
        }
    }

    /** Adds a label/value row; rows with no value are skipped. */
    private fun addRow(@StringRes label: Int, value: String?) {
        if (value.isNullOrBlank()) return
        ItemReceiptDetailRowBinding.inflate(
            LayoutInflater.from(binding.root.context), binding.llDetails, true
        ).apply {
            tvLabel.setText(label)
            tvValue.text = value
        }
    }

    private fun formatDate(epochMillis: Long): String =
        SimpleDateFormat(DATE_PATTERN, Locale.forLanguageTag("id-ID")).format(Date(epochMillis))

    private fun sendResult(action: Action) {
        setFragmentResult(
            REQUEST_KEY,
            bundleOf(
                KEY_RECEIPT_ID to receipt.id,
                KEY_ACTION to action.name,
                KEY_CATEGORY to selectedCategory.name,
            )
        )
        dismiss()
    }

    companion object {
        const val TAG = "ReceiptDetailSheet"
        const val REQUEST_KEY = "receipt_detail_result"
        private const val KEY_RECEIPT_ID = "receipt_id"
        private const val KEY_ACTION = "action"
        private const val KEY_CATEGORY = "category"
        private const val ARG_RECEIPT = "receipt"
        private const val STATE_CATEGORY = "state_category"
        private const val DATE_PATTERN = "d MMM yyyy, HH:mm"

        fun newInstance(receipt: Receipt) = ReceiptDetailSheet().apply {
            arguments = bundleOf(ARG_RECEIPT to ReceiptArgs.from(receipt))
        }

        /** Reads a result bundle from [REQUEST_KEY]. */
        fun parseResult(bundle: Bundle) = Result(
            receiptId = requireNotNull(bundle.getString(KEY_RECEIPT_ID)),
            action = Action.valueOf(requireNotNull(bundle.getString(KEY_ACTION))),
            category = bundle.getString(KEY_CATEGORY)
                ?.let { runCatching { SpendCategory.valueOf(it) }.getOrNull() }
                ?: SpendCategory.OTHERS,
        )
    }
}

/** Parcelable copy of [Receipt] for the sheet's arguments, so the domain model stays Android-free. */
@Parcelize
internal data class ReceiptArgs(
    val id: String,
    val channel: String,
    val activity: ReceiptActivity,
    val destination: String,
    val reference: String?,
    val amount: BigDecimal,
    val transactionAt: Long,
    val receivedAt: Long,
    val method: PaymentMethod?,
    val note: String?,
    val isRecorded: Boolean
) : Parcelable {
    companion object {
        fun from(r: Receipt) = ReceiptArgs(
            id = r.id,
            channel = r.channel,
            activity = r.activity,
            destination = r.destination,
            reference = r.reference,
            amount = r.amount,
            transactionAt = r.transactionAt,
            receivedAt = r.receivedAt,
            method = r.method,
            note = r.note,
            isRecorded = r.isRecorded,
        )
    }
}