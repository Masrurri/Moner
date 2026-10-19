package com.alviy.moner.feature.home.ui.model

import com.alviy.moner.core.domain.model.TransactionItem
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

sealed interface RecentTransactionRow {
    data class DateHeader(val label: String) : RecentTransactionRow
    data class Transaction(val item: TransactionItem) : RecentTransactionRow
}

/** Groups newest-first transactions under a "Today" / "Yesterday" / "d MMMM yyyy" header per day. */
fun List<TransactionItem>.toRecentTransactionRows(today: LocalDate = LocalDate.now()): List<RecentTransactionRow> =
    buildList {
        var lastLabel: String? = null
        this@toRecentTransactionRows.forEach { txn ->
            val label = dayLabel(txn.date, today)
            if (label != lastLabel) {
                add(RecentTransactionRow.DateHeader(label))
                lastLabel = label
            }
            add(RecentTransactionRow.Transaction(txn))
        }
    }

private fun dayLabel(dateIso: String, today: LocalDate): String {
    val date = runCatching { LocalDateTime.parse(dateIso).toLocalDate() }.getOrNull() ?: return ""
    return when (date) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> date.format(DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault()))
    }
}
