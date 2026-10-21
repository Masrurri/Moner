package com.alviy.moner.feature.receipt.gather.ui.model

import com.alviy.moner.api.gmail.mapper.parse.ReceiptDateFormat
import com.alviy.moner.core.domain.model.Receipt
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

sealed interface ReceiptListModel {
    data class DateHeader(val label: String) : ReceiptListModel
    data class ReceiptItem(val item: Receipt) : ReceiptListModel
}

/** Groups newest-first receipts under a "Today" / "Yesterday" / "d MMMM yyyy" header per day (WIB). */
fun List<Receipt>.toReceiptListUi(today: LocalDate = LocalDate.now(ReceiptDateFormat.ZONE)): List<ReceiptListModel> =
    buildList {
        var lastLabel: String? = null
        this@toReceiptListUi.forEach { receipt ->
            val label = dayLabel(receipt.transactionAt, today)
            if (label != lastLabel) {
                add(ReceiptListModel.DateHeader(label))
                lastLabel = label
            }
            add(ReceiptListModel.ReceiptItem(receipt))
        }
    }

private val outputFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault())

private fun dayLabel(millis: Long, today: LocalDate): String =
    when (val date = Instant.ofEpochMilli(millis).atZone(ReceiptDateFormat.ZONE).toLocalDate()) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(outputFormatter)
    }
