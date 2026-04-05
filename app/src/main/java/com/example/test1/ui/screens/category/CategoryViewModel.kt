package com.example.test1.ui.screens.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.test1.domain.model.Category
import com.example.test1.domain.model.TransactionType
import com.example.test1.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val expenseCategories: List<Category> = emptyList(),
    val incomeCategories: List<Category> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val getAllCategoriesUseCase: GetAllCategoriesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState: StateFlow<CategoryUiState> = _uiState.asStateFlow()

    private var categoriesCache: List<Category> = emptyList()

    fun loadCategories() {
        if (categoriesCache.isNotEmpty()) {
            _uiState.update {
                it.copy(
                    categories = categoriesCache,
                    expenseCategories = categoriesCache.filter { c -> c.type == TransactionType.EXPENSE },
                    incomeCategories = categoriesCache.filter { c -> c.type == TransactionType.INCOME },
                    isLoading = false
                )
            }
            return
        }
        
        viewModelScope.launch {
            getAllCategoriesUseCase().collect { categories ->
                categoriesCache = categories
                _uiState.update {
                    it.copy(
                        categories = categories,
                        expenseCategories = categories.filter { c -> c.type == TransactionType.EXPENSE },
                        incomeCategories = categories.filter { c -> c.type == TransactionType.INCOME },
                        isLoading = false
                    )
                }
            }
        }
    }
}