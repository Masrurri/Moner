package com.alviy.core.utils

import android.os.Bundle
import android.view.View
import android.widget.EditText
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.navigation.NavController
import androidx.navigation.navOptions
import com.alviy.core.database.entity.CategorySummaryEntity
import com.alviy.core.database.entity.ExpenseCategoryEntity
import com.alviy.core.database.entity.ExpenseCategoryWithTransactions
import com.alviy.core.database.entity.ExpenseSummaryEntity
import com.alviy.core.model.CategorySummaryData
import com.alviy.core.model.ExpenseItem
import com.alviy.core.model.ExpenseSummary
import com.alviy.core.model.TransactionItem
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

object Utils {
    fun fullMonthYearFormatter(locale: Locale = Locale.getDefault()): DateTimeFormatter? =
        DateTimeFormatter.ofPattern("MMMM yyyy", locale)
    fun NavController.navigateTo(
        resId: Int,
        popUpToId: Int? = null,
        isLaunchSingleTop: Boolean? = null,
        isInclusive: Boolean? = null,
        isSaveState: Boolean? = null,
        bundle: Bundle? = null
    ) {
        val navOptions = navOptions {
            popUpToId?.let {
                popUpTo(it){
                    this.inclusive = isInclusive.orFalse()
                    this.saveState = isSaveState.orFalse()
                }
            }
            this.launchSingleTop = isLaunchSingleTop.orFalse()
        }
        this.navigate(resId, bundle, navOptions)
    }

    fun Boolean?.orTrue() = this?:true
    fun Boolean?.orFalse() = this?:false

    fun View.getDrawable(@DrawableRes resId: Int) = context.getDrawable(resId)

    fun View.getColor(@ColorRes resId: Int) = context.getColor(resId)

    fun LocalDateTime.toDateMonthYear(locale: Locale = Locale.getDefault()): String {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yy", locale)
        return this.format(formatter)
    }

    fun LocalDateTime.toFullMonthYear(locale: Locale = Locale.getDefault()): String {
        return this.format(fullMonthYearFormatter(locale))
    }

    fun String.toLocalDateTime(pattern: String = "yyyy-MM-dd HH:mm:ss"): LocalDateTime {
        val formatter = DateTimeFormatter.ofPattern(pattern)
        return LocalDateTime.parse(this, formatter)
    }

    fun ExpenseCategoryWithTransactions.toExpenseItem(): ExpenseItem {
        return ExpenseItem(
            categoryId = category.categoryId,
            categoryName = category.categoryName,
            monthYear = category.monthYear,
            totalExpense = totalExpenseCategory,
            dateCreated = category.dateCreated,
            transactions = transactions.map {
                TransactionItem(
                    categoryId = it.categoryId,
                    date = it.date,
                    desc = it.desc,
                    source = it.source,
                    amount = it.amount
                )
            }
        )
    }

    fun ExpenseCategoryEntity.toExpenseItem(): ExpenseItem {
        return ExpenseItem(
            categoryName = this.categoryName,
            dateCreated = this.dateCreated,
            transactions = null
        )
    }

    fun ExpenseSummaryEntity?.toExpenseSummary(): ExpenseSummary {
        return if (this == null) ExpenseSummary()
        else ExpenseSummary(
            monthYear = this.monthYear,
            totalExpense = this.totalExpense,
            latestExpense = this.latestExpense,
            latestTransactionDate = this.latestTransactionDate
        )
    }

    fun CategorySummaryEntity?.toCategorySummaryData(): CategorySummaryData {
        return if (this == null) CategorySummaryData()
        else CategorySummaryData(
            monthYear = this.monthYear,
            categoryId = this.categoryId,
            totalExpense = this.totalExpense,
            latestTrxDate = this.latestTrxDate
        )
    }

    fun List<ExpenseCategoryWithTransactions>.toListExpenseItem(): List<ExpenseItem> {
        return map { expenseData -> expenseData.toExpenseItem() }
    }

    fun BigDecimal?.toIDR(): String {
        val localeID = Locale("in", "ID")
        val formatter = NumberFormat.getNumberInstance(localeID)
        formatter.maximumFractionDigits = 0

        return if (this == null) "Rp 0" else "Rp ${formatter.format(this)}"
    }

    fun EditText.enableIDRFormat() {
        this.addTextChangedListener(TextWatcherIDR(this))
    }

    fun String.IdrToBigDecimal(): BigDecimal {
        val clean = this
            .replace("Rp", "")
            .replace("[^\\d]".toRegex(), "")

        return clean.toBigDecimalOrNull() ?: BigDecimal.ZERO
    }

}