package com.alviy.moner.api.gmail.parser.template

import com.alviy.moner.api.gmail.domain.model.PaymentMethod
import com.alviy.moner.api.gmail.domain.model.ReceiptTransaction
import com.alviy.moner.api.gmail.parser.text.EmailText
import com.alviy.moner.api.gmail.parser.text.Money
import com.alviy.moner.api.gmail.parser.text.ParsedDate
import com.alviy.moner.api.gmail.parser.text.ReceiptDateFormat

/** One fetched email, decoded and ready for a template. */
class MessageContext(
    val id: String,                 // Gmail message id
    val from: String,
    val subject: String,
    val text: EmailText,
    val fallbackMillis: Long,       // Gmail's own receive time; used when the email has no parseable date
)

/**
 * One template per sender/layout. Return null when the email is not a payment (it is then skipped).
 * To support a new sender: add a template in this package and register it in ReceiptSources.
 */
fun interface ReceiptTemplate {
    fun parse(ctx: MessageContext, channel: String): ReceiptTransaction?
}

/** Blank, "-" and "–" mean "no value" in these emails. */
internal fun String?.meaningful(): String? = this?.trim()?.takeIf { it.isNotEmpty() && it !in EMPTY_MARKERS }

private val EMPTY_MARKERS = setOf("-", "–")

/** Builds the result with one consistent date/time rule for every template. */
internal fun MessageContext.build(
    channel: String,
    title: String,
    destination: String,
    money: Money,
    parsedDate: ParsedDate?,
    method: PaymentMethod?,
    note: String?,
): ReceiptTransaction {
    val millis = parsedDate?.millis ?: fallbackMillis      // the email's own date, else Gmail's receive time
    return ReceiptTransaction(
        id = id,
        channel = channel,
        title = title,
        destination = destination,
        amount = money.value,
        date = ReceiptDateFormat.date(millis),
        time = if (parsedDate == null || parsedDate.hasTime) ReceiptDateFormat.time(millis) else null,
        method = method,
        note = note,
    )
}
