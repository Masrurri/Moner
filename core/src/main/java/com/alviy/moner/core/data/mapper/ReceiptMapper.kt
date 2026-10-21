package com.alviy.moner.core.data.mapper

import com.alviy.moner.core.data.local.entity.ReceiptEntity
import com.alviy.moner.core.domain.model.PaymentMethod
import com.alviy.moner.core.domain.model.Receipt
import com.alviy.moner.core.domain.model.ReceiptActivity

fun Receipt.toEntity(): ReceiptEntity = ReceiptEntity(
    id = id,
    channel = channel,
    activity = activity.name,
    destination = destination,
    reference = reference,
    amount = amount,
    transactionAt = transactionAt,
    receivedAt = receivedAt,
    method = method?.name,
    note = note,
    isRecorded = isRecorded,
    isDeleted = isDeleted,
)

// Unknown names (an enum entry renamed later) degrade to OTHER / null instead of crashing.
fun ReceiptEntity.toReceipt(): Receipt = Receipt(
    id = id,
    channel = channel,
    activity = ReceiptActivity.entries.firstOrNull { it.name == activity } ?: ReceiptActivity.OTHER,
    destination = destination,
    reference = reference,
    amount = amount,
    transactionAt = transactionAt,
    receivedAt = receivedAt,
    method = PaymentMethod.entries.firstOrNull { it.name == method },
    note = note,
    isRecorded = isRecorded,
    isDeleted = isDeleted,
)
