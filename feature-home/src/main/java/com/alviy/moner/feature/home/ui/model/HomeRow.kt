package com.alviy.moner.feature.home.ui.model

import com.alviy.moner.core.common.ui.UiText
import com.alviy.moner.core.domain.model.TransactionItem

/** One row of the Home transaction list: a day header or a transaction under it. */
sealed interface HomeRow {
    data class DateHeader(val label: UiText) : HomeRow
    data class Transaction(val item: TransactionItem) : HomeRow
}
