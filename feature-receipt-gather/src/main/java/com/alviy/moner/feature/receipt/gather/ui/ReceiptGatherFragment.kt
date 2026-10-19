package com.alviy.moner.feature.receipt.gather.ui

import androidx.fragment.app.viewModels
import com.alviy.moner.core.common.base.BaseFragment
import com.alviy.moner.feature.receipt.gather.databinding.FragmentReceiptGatherBinding
import com.alviy.moner.feature.receipt.gather.viewmodel.ReceiptGatherViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReceiptGatherFragment : BaseFragment<FragmentReceiptGatherBinding>(FragmentReceiptGatherBinding::inflate) {

    private val viewModel: ReceiptGatherViewModel by viewModels()

    override fun initView() {
        observeNavigation(viewModel)
    }
}
