package com.alviy.moner.core.domain.model

import java.math.BigDecimal

/** One payment parsed from an email receipt, waiting for the user to record it as a spend (or delete it). */
data class Receipt(
    val id: String,                 // Gmail message id: primary key, so a re-fetch never duplicates
    val channel: String,            // sender: Mandiri, BCA, Grab, Others
    val activity: ReceiptActivity,
    val destination: String,        // merchant, recipient, restaurant or drop-off; the channel when unknown
    val reference: String?,         // reference no., or the destination account/customer id
    val amount: BigDecimal,         // IDR, exact
    val transactionAt: Long,        // epoch millis; missing date/time parts come from receivedAt
    val receivedAt: Long,           // Gmail receive time, epoch millis
    val method: PaymentMethod?,     // null when the email doesn't say
    val note: String?,
    val isRecorded: Boolean = false,
    val isDeleted: Boolean = false,
)

enum class ReceiptActivity(val label: String) {
    PAYMENT("Pembayaran"),
    TRANSFER("Transfer"),
    TOP_UP("Top-up"),
    GRAB_BIKE("GrabBike"),
    GRAB_CAR("GrabCar"),
    GRAB_FOOD("GrabFood"),
    OTHER("Lainnya"),
}

enum class PaymentMethod(val label: String) {
    QRIS("QRIS"),
    DEBIT("Debit"),
    CREDIT_CARD("Credit card"),
    ATM("ATM"),
    EWALLET("E-Wallet"),
}
