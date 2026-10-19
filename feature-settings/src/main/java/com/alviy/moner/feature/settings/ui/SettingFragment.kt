package com.alviy.moner.feature.settings.ui

import android.content.pm.PackageManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.alviy.moner.core.BuildConfig
import com.alviy.moner.core.common.base.BaseFragment
import com.alviy.moner.feature.settings.R
import com.alviy.moner.feature.settings.databinding.FragmentSettingBinding
import com.alviy.moner.feature.settings.viewmodel.SettingViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingFragment : BaseFragment<FragmentSettingBinding>() {

    private val viewModel: SettingViewModel by viewModels()

    override val injectedFragment = this

    override fun initBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSettingBinding.inflate(inflater, container, false)

    override fun initView() {
        binding.rowGenerateSample.isVisible(BuildConfig.DEBUG)
        binding.dividerGenerateSample.isVisible(BuildConfig.DEBUG)

        binding.switchNotifications.setOnCheckedChangeListener(null)
        binding.switchNotifications.isChecked = viewModel.appPreferences.isNotificationsEnabled
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            viewModel.appPreferences.isNotificationsEnabled = isChecked
        }

        binding.rowImportExcel.setOnClickListener { showComingSoon() }
        binding.rowExportExcel.setOnClickListener { showComingSoon() }
        binding.rowBackupRestore.setOnClickListener { showComingSoon() }

        binding.rowGenerateSample.setOnClickListener {
            viewModel.generateSampleData()
        }

        binding.rowClearDatabase.setOnClickListener {
            confirmClearDatabase()
        }

        binding.tvAppVersion.text = getString(R.string.app_version_format, appVersionName())
    }

    override fun observeData() = Unit

    private fun confirmClearDatabase() {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.settings_clear_database_confirm_title)
            .setMessage(R.string.settings_clear_database_confirm_message)
            .setPositiveButton(R.string.settings_clear_database_confirm_positive) { _, _ ->
                viewModel.clearAllTable()
            }
            .setNegativeButton(R.string.settings_clear_database_confirm_negative, null)
            .show()
    }

    private fun showComingSoon() {
        Toast.makeText(requireContext(), "Coming soon", Toast.LENGTH_SHORT).show()
    }

    private fun appVersionName(): String = try {
        requireContext().packageManager
            .getPackageInfo(requireContext().packageName, 0)
            .versionName ?: "1.0"
    } catch (e: PackageManager.NameNotFoundException) {
        "1.0"
    }

    private fun View.isVisible(visible: Boolean) {
        visibility = if (visible) View.VISIBLE else View.GONE
    }
}
