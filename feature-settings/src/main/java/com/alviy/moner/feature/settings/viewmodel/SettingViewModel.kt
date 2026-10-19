package com.alviy.moner.feature.settings.viewmodel

import com.alviy.moner.core.common.AppPreferences
import com.alviy.moner.core.common.base.BaseViewModel
import com.alviy.moner.core.designsystem.SpendCategory
import com.alviy.moner.core.domain.model.TransactionItem
import com.alviy.moner.core.domain.repository.SpendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.TemporalAdjusters
import java.time.temporal.WeekFields
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingViewModel @Inject constructor(
    private val spendRepository: SpendRepository,
    val appPreferences: AppPreferences
) : BaseViewModel() {

    fun generateSampleData() = launchSafely {
        sampleTransactions().forEach {
            spendRepository.addSpend(
                categoryName = it.categoryName,
                date = it.date,
                desc = it.desc,
                source = it.source,
                amount = it.amount
            )
        }
    }

    fun clearAllTable() = launchSafely { spendRepository.clearAllTable() }

    /**
     * Sample spends in the current month, spread so Home shows three different totals:
     * today = 67.000, this week = today + 150.000, this month = week + 275.000.
     * Earlier dates are clamped to this month, so on the 1st some of them land on today.
     */
    private fun sampleTransactions(now: LocalDateTime = LocalDateTime.now()): List<TransactionItem> {
        val today = now.toLocalDate()
        val firstOfMonth = today.withDayOfMonth(1)
        val weekStart = today.with(TemporalAdjusters.previousOrSame(WeekFields.of(Locale.getDefault()).firstDayOfWeek))
        val earlierThisWeek = maxOf(weekStart, firstOfMonth).let { if (it < today) it else today }
        val earlierThisMonth = if (firstOfMonth < earlierThisWeek) firstOfMonth else earlierThisWeek

        return listOf(
            sample(SpendCategory.FOOD, "Nasi Padang", "QRIS", 45_000, now),
            sample(SpendCategory.TRANSPORT, "Gojek Ride", "Cash", 22_000, maxOf(today.atStartOfDay(), now.minusHours(2))),
            sample(SpendCategory.SHOPPING, "Groceries", "Debit", 150_000, earlierThisWeek.atNoonOrNow(now)),
            sample(SpendCategory.VEHICLE, "Oil Change", "Debit", 275_000, earlierThisMonth.atNoonOrNow(now))
        )
    }

    private fun sample(category: SpendCategory, desc: String, source: String, amount: Long, date: LocalDateTime) =
        TransactionItem(
            categoryName = category.label,
            date = date.toString(),
            desc = desc,
            source = source,
            amount = amount.toBigDecimal()
        )

    // Noon on a past day; on today itself never later than now.
    private fun LocalDate.atNoonOrNow(now: LocalDateTime): LocalDateTime = minOf(atTime(12, 0), now)
}
