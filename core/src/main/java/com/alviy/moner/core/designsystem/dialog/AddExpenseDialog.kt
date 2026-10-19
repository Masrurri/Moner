package com.alviy.moner.core.designsystem.dialog

import android.os.Bundle
import androidx.annotation.StringRes
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.LinearLayoutManager
import com.alviy.moner.core.R
import com.alviy.moner.core.common.base.BaseBottomSheetDialogFragment
import com.alviy.moner.core.common.defaults.ExpenseDefaults
import com.alviy.moner.core.common.ext.DatePatterns
import com.alviy.moner.core.common.ext.format
import com.alviy.moner.core.common.ext.onDialogResult
import com.alviy.moner.core.common.ext.sendDialogResult
import com.alviy.moner.core.common.ext.showDialog
import com.alviy.moner.core.common.ext.showToast
import com.alviy.moner.core.databinding.DialogAddExpenseBinding
import com.alviy.moner.core.designsystem.adapter.PillAdapter
import com.google.android.material.datepicker.MaterialDatePicker
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

/** What the user entered in [AddExpenseDialog]. */
data class NewExpenseInput(
    val categoryName: String,
    val title: String,
    val source: String,
    val amount: BigDecimal,
    val date: LocalDateTime,
    val note: String?,
)

/**
 * Add Expense sheet. Reports through the Fragment Result API (see [parseResult]):
 *  - [Result.CategoryCreated] when the user adds a category from the sheet,
 *  - [Result.Saved] when the expense is valid and saved.
 */
class AddExpenseDialog : BaseBottomSheetDialogFragment<DialogAddExpenseBinding>(DialogAddExpenseBinding::inflate) {

    sealed interface Result {
        data class CategoryCreated(val name: String) : Result
        data class Saved(val input: NewExpenseInput) : Result
    }

    private val requestKey: String get() = requireArguments().getString(ARG_REQUEST_KEY).orEmpty()

    private lateinit var categories: List<String>
    private var selectedDate: LocalDateTime = LocalDateTime.now()

    private lateinit var categoryAdapter: PillAdapter
    private lateinit var sourceAdapter: PillAdapter

    override fun initView(savedInstanceState: Bundle?) {
        categoryAdapter = PillAdapter()
        sourceAdapter = PillAdapter()
        restoreState(savedInstanceState)

        binding.rvCategory.layoutManager = horizontalLayoutManager()
        binding.rvCategory.adapter = categoryAdapter
        categoryAdapter.submitList(categories)

        binding.rvSource.layoutManager = horizontalLayoutManager()
        binding.rvSource.adapter = sourceAdapter
        sourceAdapter.submitList(ExpenseDefaults.PAYMENT_SOURCES)

        updateDateLabel()
        binding.rowDate.setOnClickListener { showDatePicker() }
        binding.btnNewCategory.setOnClickListener {
            TextFieldDialog.newInstance(REQUEST_NEW_CATEGORY).showDialog(childFragmentManager, TextFieldDialog.TAG)
        }
        onDialogResult(REQUEST_NEW_CATEGORY) { result ->
            TextFieldDialog.enteredText(result)?.let(::onCategoryCreated)
        }
        binding.btnSaveExpense.setOnClickListener { submit() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (!::categoryAdapter.isInitialized) return
        outState.putStringArrayList(STATE_CATEGORIES, ArrayList(categories))
        outState.putString(STATE_SELECTED_CATEGORY, categoryAdapter.selectedItem)
        outState.putString(STATE_SELECTED_SOURCE, sourceAdapter.selectedItem)
        outState.putString(STATE_DATE, selectedDate.toString())
    }

    private fun restoreState(savedInstanceState: Bundle?) {
        categories = savedInstanceState?.getStringArrayList(STATE_CATEGORIES)
            ?: requireArguments().getStringArrayList(ARG_CATEGORIES).orEmpty()
        savedInstanceState ?: return
        categoryAdapter.selectedItem = savedInstanceState.getString(STATE_SELECTED_CATEGORY)
        sourceAdapter.selectedItem = savedInstanceState.getString(STATE_SELECTED_SOURCE)
        savedInstanceState.getString(STATE_DATE)?.let { selectedDate = LocalDateTime.parse(it) }
    }

    private fun onCategoryCreated(name: String) {
        categories = categories + name
        categoryAdapter.submitList(categories)
        categoryAdapter.selectedItem = name
        sendDialogResult(requestKey, bundleOf(RESULT_NEW_CATEGORY to name))
    }

    private fun submit() {
        val category = categoryAdapter.selectedItem
        val title = binding.tfTitle.text
        val source = sourceAdapter.selectedItem
        val amount = binding.amountInput.amount

        val error = when {
            category.isNullOrBlank() -> R.string.add_expense_error_category
            title.isBlank() -> R.string.add_expense_error_title
            source.isNullOrBlank() -> R.string.add_expense_error_source
            amount.signum() == 0 -> R.string.add_expense_error_amount
            else -> null
        }
        if (error != null) return showValidationError(error)

        sendDialogResult(
            requestKey,
            bundleOf(
                RESULT_CATEGORY to category,
                RESULT_TITLE to title,
                RESULT_SOURCE to source,
                RESULT_AMOUNT to amount.toPlainString(),
                RESULT_DATE to selectedDate.toString(),
                RESULT_NOTE to binding.tfNote.text.ifBlank { null },
            )
        )
        dismissAllowingStateLoss()
    }

    private fun showValidationError(@StringRes messageRes: Int) = requireContext().showToast(messageRes)

    private fun horizontalLayoutManager() =
        LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setSelection(selectedDate.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
            .build()
        picker.addOnPositiveButtonClickListener { millis ->
            val localDate = Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
            selectedDate = LocalDateTime.of(localDate, selectedDate.toLocalTime())
            updateDateLabel()
        }
        picker.show(childFragmentManager, DATE_PICKER_TAG)
    }

    private fun updateDateLabel() {
        val date = selectedDate.toLocalDate()
        val label = date.format(DatePatterns.DAY_MONTH_YEAR)
        binding.tvDate.text = if (date == LocalDate.now()) {
            getString(R.string.add_expense_today_format, label)
        } else {
            label
        }
    }

    companion object {
        const val TAG = "AddExpenseDialog"
        private const val DATE_PICKER_TAG = "date_picker"
        private const val REQUEST_NEW_CATEGORY = "add_expense_new_category"

        private const val ARG_CATEGORIES = "arg_categories"
        private const val ARG_REQUEST_KEY = "arg_request_key"

        private const val STATE_CATEGORIES = "state_categories"
        private const val STATE_SELECTED_CATEGORY = "state_selected_category"
        private const val STATE_SELECTED_SOURCE = "state_selected_source"
        private const val STATE_DATE = "state_date"

        private const val RESULT_NEW_CATEGORY = "result_new_category"
        private const val RESULT_CATEGORY = "result_category"
        private const val RESULT_TITLE = "result_title"
        private const val RESULT_SOURCE = "result_source"
        private const val RESULT_AMOUNT = "result_amount"
        private const val RESULT_DATE = "result_date"
        private const val RESULT_NOTE = "result_note"

        fun newInstance(availableCategories: List<String>, requestKey: String) = AddExpenseDialog().apply {
            arguments = bundleOf(
                ARG_CATEGORIES to ArrayList(availableCategories),
                ARG_REQUEST_KEY to requestKey,
            )
        }

        fun parseResult(bundle: Bundle): Result? {
            bundle.getString(RESULT_NEW_CATEGORY)?.let { return Result.CategoryCreated(it) }
            return Result.Saved(
                NewExpenseInput(
                    categoryName = bundle.getString(RESULT_CATEGORY) ?: return null,
                    title = bundle.getString(RESULT_TITLE) ?: return null,
                    source = bundle.getString(RESULT_SOURCE) ?: return null,
                    amount = bundle.getString(RESULT_AMOUNT)?.toBigDecimalOrNull() ?: return null,
                    date = bundle.getString(RESULT_DATE)?.let(LocalDateTime::parse) ?: return null,
                    note = bundle.getString(RESULT_NOTE),
                )
            )
        }
    }
}
