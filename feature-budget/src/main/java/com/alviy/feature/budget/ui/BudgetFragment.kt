package com.alviy.feature.budget.ui

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.alviy.app.feature.budget.databinding.FragmentBudgetBinding
import com.alviy.core.base.BaseFragment
import com.alviy.feature.budget.viewmodel.BudgetViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BudgetFragment : BaseFragment<FragmentBudgetBinding>(
    FragmentBudgetBinding::inflate
) {

    private val viewModel: BudgetViewModel by viewModels()

    override val injectedFragment = this

    override fun initView() {
    }

    override fun observeData() {
        // observe ViewModel
    }
}