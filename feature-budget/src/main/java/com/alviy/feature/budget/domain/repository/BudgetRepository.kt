package com.alviy.feature.budget.domain.repository

import com.alviy.feature.budget.data.remote.request.BudgetRequest
import com.alviy.feature.budget.data.remote.response.BudgetRessponse
import okhttp3.Response

interface BudgetRepository {

    suspend fun getBudgetData(): BudgetRessponse

    suspend fun postBudget(request: BudgetRequest): Response
}