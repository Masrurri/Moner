package com.alviy.feature.budget.data.remote.api

import com.alviy.feature.budget.data.remote.request.BudgetRequest
import com.alviy.feature.budget.data.remote.response.BudgetRessponse
import okhttp3.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface BudgetApi {

    @GET("budget")
    suspend fun getBudgetData(): BudgetRessponse

    @POST("home")
    suspend fun postBudget(
        @Body request: BudgetRequest
    ): Response
}