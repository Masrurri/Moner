package com.alviy.moner.feature.settings.ui

import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.alviy.moner.core.BuildConfig
import com.alviy.moner.core.common.base.BaseFragment
import com.alviy.moner.core.common.ext.appVersionName
import com.alviy.moner.core.common.ext.showToast
import com.alviy.moner.feature.settings.R
import com.alviy.moner.feature.settings.databinding.FragmentSettingBinding
import com.alviy.moner.feature.settings.viewmodel.SettingViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.alviy.moner.core.R as CoreR

@AndroidEntryPoint
class SettingFragment : BaseFragment<FragmentSettingBinding>(FragmentSettingBinding::inflate) {

    private val viewModel: SettingViewModel by viewModels()

    override fun initView() = with(binding) {
        rowGenerateSample.isVisible = BuildConfig.DEBUG
        dividerGenerateSample.isVisible = BuildConfig.DEBUG

        switchDarkMode.setOnCheckedChangeListener(null)
        switchDarkMode.isChecked = viewModel.isDarkMode
        switchDarkMode.setOnCheckedChangeListener { _, isChecked -> viewModel.setDarkMode(isChecked) }

        switchNotifications.setOnCheckedChangeListener(null)
        switchNotifications.isChecked = viewModel.isNotificationsEnabled
        switchNotifications.setOnCheckedChangeListener { _, isChecked -> viewModel.setNotificationsEnabled(isChecked) }

        listOf(rowImportExcel, rowExportExcel, rowBackupRestore).forEach { row ->
            row.setOnClickListener { requireContext().showToast(CoreR.string.common_coming_soon) }
        }
        rowGenerateSample.setOnClickListener { viewModel.generateSampleData() }
        rowClearDatabase.setOnClickListener { confirmClearDatabase() }

        tvAppVersion.text = getString(
            R.string.app_version_format,
            requireContext().appVersionName(fallback = getString(R.string.app_version_fallback))
        )
    }

    private fun confirmClearDatabase() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.settings_clear_database_confirm_title)
            .setMessage(R.string.settings_clear_database_confirm_message)
            .setPositiveButton(R.string.settings_clear_database_confirm_positive) { _, _ -> viewModel.clearAllTable() }
            .setNegativeButton(CoreR.string.common_cancel, null)
            .show()
    }
}
