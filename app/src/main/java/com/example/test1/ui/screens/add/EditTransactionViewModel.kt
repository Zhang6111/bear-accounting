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

data class EditTransactionUiState(
    val transactionId: Long = 0,
    val type: TransactionType = TransactionType.EXPENSE,
    val amount: Double = 0.0,
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class EditTransactionViewModel @Inject constructor(
    private val getTransactionByIdUseCase: GetTransactionByIdUseCase,
    private val getCategoriesByTypeUseCase: GetCategoriesByTypeUseCase,
    private val updateTransactionUseCase: UpdateTransactionUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditTransactionUiState())
    val uiState: StateFlow<EditTransactionUiState> = _uiState.asStateFlow()

    fun loadTransaction(id: Long) {
        viewModelScope.launch {
            val transaction = getTransactionByIdUseCase(id)
            if (transaction != null) {
                _uiState.update {
                    it.copy(
                        transactionId = transaction.id,
                        type = transaction.type,
                        amount = transaction.amount,
                        selectedCategoryId = transaction.categoryId,
                        date = transaction.date,
                        note = transaction.note,
                        isLoading = false
                    )
                }
                loadCategories()
            }
        }
    }

    fun loadCategories() {
        viewModelScope.launch {
            getCategoriesByTypeUseCase(_uiState.value.type.value).collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun updateType(type: TransactionType) {
        _uiState.update { it.copy(type = type, selectedCategoryId = null) }
        loadCategories()
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

    fun updateTransaction() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.amount > 0 && state.selectedCategoryId != null) {
                val transaction = Transaction(
                    id = state.transactionId,
                    amount = state.amount,
                    type = state.type,
                    categoryId = state.selectedCategoryId!!,
                    note = state.note,
                    date = state.date
                )
                updateTransactionUseCase(transaction)
                _uiState.update { it.copy(isSaved = true) }
            }
        }
    }

    fun deleteTransaction() {
        viewModelScope.launch {
            deleteTransactionUseCase(_uiState.value.transactionId)
            _uiState.update { it.copy(isDeleted = true) }
            onNavigateBack()
        }
    }

    private fun onNavigateBack() {
        _uiState.update { it.copy(isDeleted = true) }
    }
}