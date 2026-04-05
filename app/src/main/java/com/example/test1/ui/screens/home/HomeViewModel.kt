package com.example.test1.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test1.domain.model.Category
import com.example.test1.domain.model.Transaction
import com.example.test1.domain.model.TransactionType
import com.example.test1.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class HomeUiState(
    val transactions: List<Transaction> = emptyList(),
    val categories: List<Category> = emptyList(),
    val monthIncome: Double = 0.0,
    val monthExpense: Double = 0.0,
    val isLoading: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllTransactionsUseCase: GetAllTransactionsUseCase,
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase,
    private val getTotalByTypeUseCase: GetTotalByTypeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var categoriesCache: List<Category> = emptyList()
    private var transactionsCache: List<Transaction> = emptyList()

    fun loadData() {
        if (categoriesCache.isNotEmpty()) return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            val (startOfMonth, endOfMonth) = getCurrentMonthRange()
            
            combine(
                getAllCategoriesUseCase(),
                getAllTransactionsUseCase(),
                getTotalByTypeUseCase(TransactionType.INCOME.value, startOfMonth, endOfMonth),
                getTotalByTypeUseCase(TransactionType.EXPENSE.value, startOfMonth, endOfMonth)
            ) { categories, transactions, income, expense ->
                categoriesCache = categories
                transactionsCache = transactions
                
                HomeUiState(
                    transactions = transactions,
                    categories = categories,
                    monthIncome = income ?: 0.0,
                    monthExpense = expense ?: 0.0,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    private fun getCurrentMonthRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfMonth = calendar.timeInMillis
        
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val endOfMonth = calendar.timeInMillis
        
        return Pair(startOfMonth, endOfMonth)
    }
}