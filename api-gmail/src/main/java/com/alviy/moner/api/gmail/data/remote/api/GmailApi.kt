package com.alviy.moner.api.gmail.data.remote.api

import com.alviy.moner.api.gmail.model.GmailListResponse
import com.alviy.moner.api.gmail.model.GmailMessage
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GmailApi {
    @GET("gmail/v1/users/me/messages")
    suspend fun listMessages(
        @Query("q") query: String,
        @Query("maxResults") maxResults: Int = 20,
        @Query("pageToken") pageToken: String? = null,
    ): Response<GmailListResponse>

    @GET("gmail/v1/users/me/messages/{id}")
    suspend fun getMessage(
        @Path("id") id: String,
        @Query("format") format: String = "full",
    ): Response<GmailMessage>
}