package com.alviy.moner.core.data.mapper

import com.alviy.moner.core.data.local.entity.ReceiptEntity
import com.alviy.moner.core.designsystem.SpendCategory
import com.alviy.moner.core.domain.model.PaymentMethod
import com.alviy.moner.core.domain.model.Receipt
import com.alviy.moner.core.domain.model.ReceiptActivity
import java.math.BigDecimal
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

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

/** Everything SpendRepository.addSpend needs to record a receipt as a transaction. */
data class NewSpend(
    val categoryName: String,
    val date: String,
    val desc: String,
    val source: String,
    val amount: BigDecimal,
    val note: String?,
    val receiptId: String,
)

/**
 * Receipt → transaction fields:
 * - categoryName: the category the user confirmed in the sheet ([SpendCategory.label], as stored on transactions)
 * - date:         transactionAt as a local LocalDateTime string, the same format addSpend parses and stores
 * - desc:         destination (merchant, recipient, restaurant…)
 * - source:       channel (Mandiri, BCA, Grab…), where the money came from
 * - note:         the receipt note and/or reference number
 * - receiptId:    links the transaction back to the receipt; addSpend also marks the receipt recorded
 */
fun Receipt.toNewSpend(category: SpendCategory, zone: ZoneId = ZoneId.systemDefault()) = NewSpend(
    categoryName = category.label,
    date = Instant.ofEpochMilli(transactionAt)
        .atZone(zone)
        .toLocalDateTime()
        .truncatedTo(ChronoUnit.SECONDS)
        .toString(),
    desc = destination,
    source = channel,
    amount = amount,
    note = listOfNotNull(note, reference?.let { "Ref: $it" })
        .joinToString("\n")
        .ifBlank { null },
    receiptId = id,
)
