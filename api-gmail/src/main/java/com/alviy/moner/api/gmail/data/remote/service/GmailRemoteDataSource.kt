package com.alviy.moner.api.gmail.data.remote.service

import com.alviy.moner.api.gmail.data.remote.api.GmailApi
import com.alviy.moner.api.gmail.model.GmailListResponse
import com.alviy.moner.api.gmail.model.GmailMessage
import com.alviy.moner.api.gmail.model.GmailProfileResponse
import com.alviy.moner.api.gmail.model.GoogleErrorEnvelope
import com.alviy.moner.core.common.base.BaseDataSource
import com.alviy.moner.core.common.entity.ErrorResponse
import com.alviy.moner.core.common.entity.Result
import com.google.gson.Gson
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GmailRemoteDataSource @Inject constructor(
    private val api: GmailApi,
    gson: Gson
) : BaseDataSource(gson) {

    suspend fun listMessages(query: String, max: Int, pageToken: String? = null): Result<GmailListResponse> =
        getApiResult { api.listMessages(query, max, pageToken) }

    suspend fun getMessage(id: String): Result<GmailMessage> =
        getApiResult { api.getMessage(id) }

    suspend fun getProfile(): Result<GmailProfileResponse> =
        getApiResult { api.getProfile() }

    override fun parseError(raw: String): ErrorResponse? =
        gson.fromJson(raw, GoogleErrorEnvelope::class.java)?.error?.let {
            ErrorResponse(code = it.code?.toString(), title = it.status, message = it.message)
        }
}