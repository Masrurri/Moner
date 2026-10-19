package com.alviy.moner.api.gmail.data.remote.api

import com.alviy.moner.api.gmail.data.remote.dto.GmailListResponse
import com.alviy.moner.api.gmail.data.remote.dto.GmailMessage
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/** Gmail REST API v1, read-only. Base URL: [com.alviy.moner.core.network.ApiBaseUrl.GMAIL]. */
interface GmailApi {

    @GET("gmail/v1/users/me/messages")
    suspend fun listMessages(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = DEFAULT_PAGE_SIZE,
        @Query("pageToken") pageToken: String? = null,
    ): Response<GmailListResponse>

    @GET("gmail/v1/users/me/messages/{id}")
    suspend fun getMessage(
        @Path("id") id: String,
        @Query("format") format: String = FORMAT_FULL,
    ): Response<GmailMessage>

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        const val FORMAT_FULL = "full"
    }
}
