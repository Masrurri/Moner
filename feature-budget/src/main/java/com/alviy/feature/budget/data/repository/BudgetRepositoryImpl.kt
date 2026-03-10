package com.alviy.feature.budget.data.repository

import com.alviy.feature.budget.data.remote.api.BudgetApi
import com.alviy.feature.budget.data.remote.request.BudgetRequest
import com.alviy.feature.budget.data.remote.response.BudgetRessponse
import com.alviy.feature.budget.domain.repository.BudgetRepository
import okhttp3.Response
import javax.inject.Inject

class BudgetRepositoryImpl @Inject constructor(
    private val api: BudgetApi
): BudgetRepository {
    override suspend fun getBudgetData(): BudgetRessponse {
        TODO("Not yet implemented")
    }

    override suspend fun postBudget(request: BudgetRequest): Response {
        TODO("Not yet implemented")
    }

}