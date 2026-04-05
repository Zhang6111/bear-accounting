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

data class CategoryEditUiState(
    val categoryId: Long = 0,
    val name: String = "",
    val icon: String = "📦",
    val color: String = "#A8D8EA",
    val type: TransactionType = TransactionType.EXPENSE,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val isLoading: Boolean = true
)

@HiltViewModel
class CategoryEditViewModel @Inject constructor(
    private val getCategoryByIdUseCase: GetCategoryByIdUseCase,
    private val addCategoryUseCase: AddCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryEditUiState())
    val uiState: StateFlow<CategoryEditUiState> = _uiState.asStateFlow()

    fun loadCategory(id: Long) {
        viewModelScope.launch {
            val category = getCategoryByIdUseCase(id)
            if (category != null) {
                _uiState.update {
                    it.copy(
                        categoryId = category.id,
                        name = category.name,
                        icon = category.icon,
                        color = category.color,
                        type = category.type,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun updateName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    fun updateIcon(icon: String) {
        _uiState.update { it.copy(icon = icon) }
    }

    fun updateColor(color: String) {
        _uiState.update { it.copy(color = color) }
    }

    fun updateType(type: TransactionType) {
        _uiState.update { it.copy(type = type) }
    }

    fun saveCategory() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.name.isNotEmpty()) {
                val category = Category(
                    id = state.categoryId,
                    name = state.name,
                    icon = state.icon,
                    color = state.color,
                    type = state.type
                )
                if (state.categoryId == 0L) {
                    addCategoryUseCase(category)
                } else {
                    updateCategoryUseCase(category)
                }
                _uiState.update { it.copy(isSaved = true) }
            }
        }
    }

    fun deleteCategory() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.categoryId != 0L) {
                val category = Category(
                    id = state.categoryId,
                    name = state.name,
                    icon = state.icon,
                    color = state.color,
                    type = state.type
                )
                deleteCategoryUseCase(category)
                _uiState.update { it.copy(isDeleted = true) }
            }
        }
    }
}