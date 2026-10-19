package com.alviy.moner.feature.receipt.gather.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.alviy.moner.core.common.base.BaseFragment
import com.alviy.moner.feature.receipt.gather.databinding.FragmentReceiptGatherBinding
import com.alviy.moner.feature.receipt.gather.viewmodel.ReceiptGatherViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReceiptGatherFragment : BaseFragment<FragmentReceiptGatherBinding>() {

    private val viewModel: ReceiptGatherViewModel by viewModels()

    override val injectedFragment = this

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentReceiptGatherBinding.inflate(inflater, container, false)

    override fun initView() {

    }

    override fun observeData() {

    }
}