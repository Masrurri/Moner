package com.alviy.feature.setting.ui

import androidx.fragment.app.viewModels
import com.alviy.app.feature.setting.databinding.FragmentSettingBinding
import com.alviy.feature.setting.viewmodel.SettingViewModel
import com.alviy.core.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue

@AndroidEntryPoint
class SettingFragment : BaseFragment<FragmentSettingBinding>(
    FragmentSettingBinding::inflate
) {

    private val viewModel: SettingViewModel by viewModels()

    override val injectedFragment = this

    override fun initView() {
        binding?.btnGenerateTemplate?.setOnClickListener {
            viewModel.generateTemplateCategory()
        }
        binding?.btnClearDb?.setOnClickListener {
            viewModel.clearAllTable()
        }
    }

    override fun observeData() {
        // observe ViewModel
    }
}