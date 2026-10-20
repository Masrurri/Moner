package com.alviy.moner.feature.receipt.gather.ui.model

import com.alviy.moner.api.gmail.model.ReceiptTransaction
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

sealed interface ReceiptListModel {
    data class DateHeader(val label: String) : ReceiptListModel
    data class ReceiptItem(val item: ReceiptTransaction) : ReceiptListModel
}

/** Groups newest-first transactions under a "Today" / "Yesterday" / "d MMMM yyyy" header per day. */
fun List<ReceiptTransaction>.toReceiptListUi(today: LocalDate = LocalDate.now()): List<ReceiptListModel> =
    buildList {
        var lastLabel: String? = null
        this@toReceiptListUi.forEach { txn ->
            val label = dayLabel(txn.date, today)
            if (label != lastLabel) {
                add(ReceiptListModel.DateHeader(label))
                lastLabel = label
            }
            add(ReceiptListModel.ReceiptItem(txn))
        }
    }

private val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val outputFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())

private fun dayLabel(dateStr: String, today: LocalDate = LocalDate.now()): String {
    val date = runCatching { LocalDate.parse(dateStr.trim(), inputFormatter) }.getOrNull() ?: return ""
    return when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(outputFormatter)
    }
}
