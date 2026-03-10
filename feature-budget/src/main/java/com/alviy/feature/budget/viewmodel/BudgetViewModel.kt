package com.alviy.feature.budget.viewmodel

import com.alviy.core.base.BaseViewModel
import com.alviy.feature.budget.domain.repository.BudgetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val repository: BudgetRepository
) : BaseViewModel() {

    fun loadData() {
        launchSafely { repository.getBudgetData() }
    }
}