package com.example.test1.ui.screens.statistics

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test1.domain.model.Category
import com.example.test1.domain.model.Transaction
import com.example.test1.domain.model.TransactionType
import com.example.test1.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import javax.inject.Inject

data class StatisticsUiState(
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val expenseByCategory: List<Pair<Long, Double>> = emptyList(),
    val incomeByCategory: List<Pair<Long, Double>> = emptyList(),
    val dailyData: List<Pair<String, Double>> = emptyList(),
    val dailyIncomeData: List<Pair<String, Double>> = emptyList(),
    val dailyExpenseData: List<Pair<String, Double>> = emptyList(),
    val selectedTimeRange: TimeRange = TimeRange.MONTH,
    val isLoading: Boolean = false,
    val exportFile: File? = null
)

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getAllTransactionsUseCase: GetAllTransactionsUseCase,
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase,
    private val getTransactionsByDateRangeUseCase: GetTransactionsByDateRangeUseCase,
    private val getTotalByTypeUseCase: GetTotalByTypeUseCase,
    private val exportDataUseCase: ExportDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private var allTransactions: List<Transaction> = emptyList()
    private var allCategories: List<Category> = emptyList()
    private var dataLoaded = false

    fun loadData() {
        if (dataLoaded && allCategories.isNotEmpty()) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            combine(
                getAllTransactionsUseCase(),
                getAllCategoriesUseCase()
            ) { transactions, categories ->
                allTransactions = transactions
                allCategories = categories
                dataLoaded = true
                
                val filtered = filterByTimeRange(transactions, _uiState.value.selectedTimeRange)
                calculateStatistics(filtered)
                
                StatisticsUiState(
                    transactions = filtered,
                    categories = categories,
                    selectedTimeRange = _uiState.value.selectedTimeRange,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun selectTimeRange(range: TimeRange) {
        _uiState.update { it.copy(selectedTimeRange = range) }
        val filtered = filterByTimeRange(allTransactions, range)
        calculateStatistics(filtered)
        _uiState.update { it.copy(transactions = filtered) }
    }

    fun exportData() {
        viewModelScope.launch {
            val state = _uiState.value
            val file = exportDataUseCase.exportToCSV(state.transactions, allCategories)
            _uiState.update { it.copy(exportFile = file) }
        }
    }

    fun getShareIntent(file: File): Intent = exportDataUseCase.shareFile(file)
    fun clearExportFile() { _uiState.update { it.copy(exportFile = null) } }

    private fun filterByTimeRange(transactions: List<Transaction>, range: TimeRange): List<Transaction> {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val startTime = when (range) {
            TimeRange.TODAY -> startOfDay.timeInMillis
            TimeRange.WEEK -> {
                startOfDay.add(Calendar.DAY_OF_YEAR, -Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 1)
                startOfDay.timeInMillis
            }
            TimeRange.MONTH -> {
                startOfDay.set(Calendar.DAY_OF_MONTH, 1)
                startOfDay.timeInMillis
            }
            TimeRange.YEAR -> {
                startOfDay.set(Calendar.DAY_OF_YEAR, 1)
                startOfDay.timeInMillis
            }
            TimeRange.ALL -> 0L
        }

        return if (startTime == 0L) transactions
        else transactions.filter { it.date >= startTime }
    }

    private fun calculateStatistics(transactions: List<Transaction>) {
        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val totalExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }

        val expenseByCategory = transactions.filter { it.type == TransactionType.EXPENSE }
            .groupBy { it.categoryId }
            .mapValues { it.value.sumOf { t -> t.amount } }
            .toList()

        val incomeByCategory = transactions.filter { it.type == TransactionType.INCOME }
            .groupBy { it.categoryId }
            .mapValues { it.value.sumOf { t -> t.amount } }
            .toList()

        val dailyData = calculateDailyData(transactions)
        val dailyIncomeData = calculateDailyData(transactions.filter { it.type == TransactionType.INCOME })
        val dailyExpenseData = calculateDailyData(transactions.filter { it.type == TransactionType.EXPENSE })

        _uiState.update {
            it.copy(
                totalIncome = totalIncome,
                totalExpense = totalExpense,
                expenseByCategory = expenseByCategory,
                incomeByCategory = incomeByCategory,
                dailyData = dailyData,
                dailyIncomeData = dailyIncomeData,
                dailyExpenseData = dailyExpenseData
            )
        }
    }

    private fun calculateDailyData(transactions: List<Transaction>): List<Pair<String, Double>> {
        val calendar = Calendar.getInstance()
        val range = _uiState.value.selectedTimeRange
        
        val days = when (range) {
            TimeRange.TODAY -> 1
            TimeRange.WEEK -> 7
            TimeRange.MONTH -> 30
            TimeRange.YEAR -> 12
            TimeRange.ALL -> 12
        }

        return if (range == TimeRange.YEAR || range == TimeRange.ALL) {
            val monthlyData = (0 until 12).map { month ->
                val monthName = arrayOf("1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月")[month]
                val total = transactions.filter { 
                    val transCalendar = Calendar.getInstance().apply { timeInMillis = it.date }
                    transCalendar.get(Calendar.MONTH) == month 
                }.sumOf { it.amount }
                monthName to total
            }
            monthlyData
        } else {
            val dailyTotals = mutableMapOf<String, Double>()
            transactions.forEach { trans ->
                calendar.timeInMillis = trans.date
                val dayLabel = when (range) {
                    TimeRange.TODAY, TimeRange.WEEK -> "${calendar.get(Calendar.DAY_OF_MONTH)}日"
                    else -> "${calendar.get(Calendar.DAY_OF_MONTH)}日"
                }
                dailyTotals[dayLabel] = (dailyTotals[dayLabel] ?: 0.0) + trans.amount
            }
            dailyTotals.toList().sortedBy { it.first }
        }
    }
}