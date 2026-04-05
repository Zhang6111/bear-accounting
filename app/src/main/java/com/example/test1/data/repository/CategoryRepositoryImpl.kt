package com.example.test1.data.repository

import com.example.test1.data.local.dao.CategoryDao
import com.example.test1.data.local.entity.CategoryEntity
import com.example.test1.domain.model.Category
import com.example.test1.domain.model.TransactionType
import com.example.test1.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao
) : CategoryRepository {

    private val allCategoriesFlow = categoryDao.getAllCategories()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(
            scope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO),
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    override fun getAllCategories(): Flow<List<Category>> = allCategoriesFlow

    override fun getCategoriesByType(type: Int): Flow<List<Category>> {
        return categoryDao.getCategoriesByType(type).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getCategoryById(id: Long): Category? {
        return categoryDao.getCategoryById(id)?.toDomain()
    }

    override suspend fun insertCategory(category: Category): Long {
        return categoryDao.insertCategory(category.toEntity())
    }

    override suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category.toEntity())
    }

    override suspend fun deleteCategory(category: Category) {
        categoryDao.deleteCategory(category.toEntity())
    }

    override suspend fun getCategoryCount(): Int {
        return categoryDao.getCategoryCount()
    }

    private fun CategoryEntity.toDomain(): Category {
        return Category(
            id = id,
            name = name,
            icon = icon,
            color = color,
            type = TransactionType.fromValue(type),
            isDefault = isDefault
        )
    }

    private fun Category.toEntity(): CategoryEntity {
        return CategoryEntity(
            id = id,
            name = name,
            icon = icon,
            color = color,
            type = type.value,
            isDefault = isDefault
        )
    }
}