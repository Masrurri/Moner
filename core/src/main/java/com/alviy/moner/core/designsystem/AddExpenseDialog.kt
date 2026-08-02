package com.alviy.moner.core.designsystem

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.alviy.moner.core.databinding.DialogAddExpenseBinding
import com.google.android.material.datepicker.MaterialDatePicker
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale

data class NewExpenseInput(
    val categoryName: String,
    val title: String,
    val source: String,
    val amount: BigDecimal,
    val date: LocalDateTime,
    val note: String?
)

class AddExpenseDialog : FullScreenSheetDialogFragment() {

    private var _binding: DialogAddExpenseBinding? = null
    private val binding get() = _binding!!

    private var selectedCategory: String? = null
    private var selectedSource: String? = null
    private var selectedDate: LocalDateTime = LocalDateTime.now()

    private var categories: List<String> = emptyList()
    private lateinit var categoryAdapter: SourceExpenseAdapter
    private lateinit var sourceAdapter: SourceExpenseAdapter

    var onSave: ((NewExpenseInput) -> Unit)? = null
    var onNewCategoryRequested: ((onCreated: (String) -> Unit) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddExpenseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryAdapter = SourceExpenseAdapter { selectedCategory = it }
        binding.rvCategory.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvCategory.adapter = categoryAdapter
        categoryAdapter.submitList(categories)

        sourceAdapter = SourceExpenseAdapter { selectedSource = it }
        sourceAdapter.submitList(PAYMENT_SOURCES)
        binding.rvSource.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvSource.adapter = sourceAdapter

        updateDateLabel()

        binding.rowDate.setOnClickListener { showDatePicker() }

        binding.btnNewCategory.setOnClickListener {
            onNewCategoryRequested?.invoke { created ->
                categories = categories + created
                categoryAdapter.submitList(categories)
                categoryAdapter.selectedCategory = created
                selectedCategory = created
            }
        }

        binding.btnSaveExpense.setOnClickListener {
            val category = selectedCategory
            val title = binding.tfTitle.getText()
            val source = selectedSource
            val amount = binding.amountInput.amount

            if (category.isNullOrBlank()) {
                showValidationError(com.alviy.moner.core.R.string.add_expense_error_category)
                return@setOnClickListener
            }
            if (title.isBlank()) {
                showValidationError(com.alviy.moner.core.R.string.add_expense_error_title)
                return@setOnClickListener
            }
            if (source.isNullOrBlank()) {
                showValidationError(com.alviy.moner.core.R.string.add_expense_error_source)
                return@setOnClickListener
            }
            if (amount == BigDecimal.ZERO) {
                showValidationError(com.alviy.moner.core.R.string.add_expense_error_amount)
                return@setOnClickListener
            }

            onSave?.invoke(
                NewExpenseInput(
                    categoryName = category,
                    title = title,
                    source = source,
                    amount = amount,
                    date = selectedDate,
                    note = binding.tfNote.getText().ifBlank { null }
                )
            )
            dismissAllowingStateLoss()
        }
    }

    private fun showValidationError(@androidx.annotation.StringRes messageRes: Int) {
        Toast.makeText(requireContext(), messageRes, Toast.LENGTH_SHORT).show()
    }

    fun setAvailableCategories(categories: List<String>) {
        this.categories = categories
        if (::categoryAdapter.isInitialized) categoryAdapter.submitList(categories)
    }

    private fun showDatePicker() {
        val picker = MaterialDatePicker.Builder.datePicker()
            .setSelection(selectedDate.toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
            .build()
        picker.addOnPositiveButtonClickListener { millis ->
            val localDate = java.time.Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate()
            selectedDate = LocalDateTime.of(localDate, selectedDate.toLocalTime())
            updateDateLabel()
        }
        picker.show(childFragmentManager, "date_picker")
    }

    private fun updateDateLabel() {
        val formatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.getDefault())
        val isToday = selectedDate.toLocalDate() == LocalDateTime.now().toLocalDate()
        binding.tvDate.text = if (isToday) {
            getString(
                com.alviy.moner.core.R.string.add_expense_today_format,
                selectedDate.toLocalDate().format(formatter)
            )
        } else {
            selectedDate.toLocalDate().format(formatter)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val PAYMENT_SOURCES = listOf("Cash", "QRIS", "Debit")

        fun newInstance(availableCategories: List<String>) = AddExpenseDialog().apply {
            categories = availableCategories
        }
    }
}
