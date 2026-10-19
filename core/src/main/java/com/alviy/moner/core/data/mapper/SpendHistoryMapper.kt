package com.alviy.moner.core.data.mapper

import com.alviy.moner.core.data.local.entity.SpendHistoryEntry
import com.alviy.moner.core.domain.model.TransactionItem

fun SpendHistoryEntry.toTransactionItem(): TransactionItem = TransactionItem(
    id = transactionId,
    categoryId = categoryId,
    categoryName = categoryName,
    date = date,
    desc = desc,
    source = source,
    amount = amount,
    note = note
)
