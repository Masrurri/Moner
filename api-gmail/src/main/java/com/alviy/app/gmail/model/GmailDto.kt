package com.alviy.app.gmail.model

data class GmailListResponse(
    val messages: List<MessageRef>?,
    val nextPageToken: String?,
)

data class MessageRef(val id: String)

data class GmailMessage(
    val id: String,
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

data class PartBody(val data: String?) // base64url

data class GoogleErrorEnvelope(val error: GoogleError?)
data class GoogleError(val code: Int?, val message: String?, val status: String?)
