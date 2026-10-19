package com.alviy.moner.api.gmail.data.remote.dto

import androidx.annotation.Keep

// Gson DTOs: field names mirror the Gmail JSON. @Keep so they survive R8 if minification is enabled.

@Keep
data class GmailListResponse(
    val messages: List<MessageRef>?,
    val nextPageToken: String?,
)

@Keep
data class MessageRef(val id: String)

@Keep
data class GmailMessage(
    val id: String,
    val internalDate: String?,      // epoch millis as String
    val payload: MessagePart?,
)

@Keep
data class MessagePart(
    val mimeType: String?,
    val headers: List<Header>?,
    val body: PartBody?,
    val parts: List<MessagePart>?,
)

@Keep
data class Header(val name: String, val value: String)

@Keep
data class PartBody(val data: String?) // base64url

@Keep
data class GoogleErrorEnvelope(val error: GoogleError?)

@Keep
data class GoogleError(val code: Int?, val message: String?, val status: String?)
