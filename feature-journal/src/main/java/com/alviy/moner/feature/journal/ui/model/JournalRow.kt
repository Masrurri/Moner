package com.alviy.moner.feature.journal.ui.model

import com.alviy.moner.core.domain.model.TransactionItem

sealed interface JournalRow {
    data class DateHeader(val label: String) : JournalRow
    data class Transaction(val item: TransactionItem) : JournalRow
}
