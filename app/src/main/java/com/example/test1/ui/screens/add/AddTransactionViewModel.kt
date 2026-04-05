package com.example.test1.ui.screens.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test1.domain.model.Category
import com.example.test1.domain.model.Transaction
import com.example.test1.domain.model.TransactionType
import com.example.test1.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AddTransactionUiState(
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: Double = 0.0,
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    val isSaved: Boolean = false,
    val isLoading: Boolean = false
)

@HiltViewModel
class AddTransactionViewModel @Inject constructor(
    private val addTransactionUseCase: AddTransactionUseCase,
    private val getCategoriesByTypeUseCase: GetCategoriesByTypeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddTransactionUiState())
    val uiState: StateFlow<AddTransactionUiState> = _uiState.asStateFlow()

    private var expenseCategories: List<Category> = emptyList()
    private var incomeCategories: List<Category> = emptyList()

    fun loadCategories() {
        viewModelScope.launch {
            combine(
                getCategoriesByTypeUseCase(TransactionType.EXPENSE.value),
                getCategoriesByTypeUseCase(TransactionType.INCOME.value)
            ) { expense, income ->
                expenseCategories = expense
                incomeCategories = income
                if (_uiState.value.type == TransactionType.EXPENSE) expense else income
            }.first().let { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun updateType(type: TransactionType) {
        _uiState.update { 
            it.copy(
                type = type, 
                selectedCategoryId = null,
                categories = if (type == TransactionType.EXPENSE) expenseCategories else incomeCategories
            ) 
        }
    }

    fun updateAmount(amount: Double) {
        _uiState.update { it.copy(amount = amount) }
    }

    fun updateCategory(categoryId: Long) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun updateDate(date: Long) {
        _uiState.update { it.copy(date = date) }
    }

    fun updateNote(note: String) {
        _uiState.update { it.copy(note = note) }
    }

    fun saveTransaction() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.amount > 0 && state.selectedCategoryId != null) {
                val transaction = Transaction(
                    amount = state.amount,
                    type = state.type,
                    categoryId = state.selectedCategoryId!!,
                    note = state.note,
                    date = state.date
                )
                addTransactionUseCase(transaction)
                _uiState.update { it.copy(isSaved = true) }
            }
        }
    }
}