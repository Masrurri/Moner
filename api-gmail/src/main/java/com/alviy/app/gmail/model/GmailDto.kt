package com.alviy.app.gmail.model

data class GmailListResponse(
    val messages: List<MessageRef>?,
    val nextPageToken: String?,
    val resultSizeEstimate: Int?,
)

data class MessageRef(val id: String, val threadId: String?)

data class GmailMessage(
    val id: String,
    val threadId: String?,
    val snippet: String?,
    val internalDate: String?,      // epoch millis as String
    val payload: MessagePart?,
)

data class MessagePart(
    val mimeType: String?,
    val headers: List<Header>?,
    val body: PartBody?,
    val parts: List<MessagePart>?,
)

data class Header(val name: String, val value: String)

data class PartBody(val size: Int?, val data: String?) // base64url

/** Google error envelope: {"error":{"code":403,"message":"...","status":"PERMISSION_DENIED"}} */
data class GoogleErrorEnvelope(val error: GoogleError?)
data class GoogleError(val code: Int?, val message: String?, val status: String?)

data class GmailReceiptTransaction(
    val id: String,
    val merchant: String,
    val subject: String,
    val amount: Double?,        // null if no amount could be parsed
    val currency: String?,      // "IDR", "USD", ...
    val dateMillis: Long,
    val snippet: String,
)
