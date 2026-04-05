package com.example.test1.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.test1.data.local.dao.CategoryDao
import com.example.test1.data.local.dao.TransactionDao
import com.example.test1.data.local.entity.CategoryEntity
import com.example.test1.data.local.entity.TransactionEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [TransactionEntity::class, CategoryEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "accounting_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    populateDatabase(database.categoryDao())
                }
            }
        }

        suspend fun populateDatabase(categoryDao: CategoryDao) {
            val expenseCategories = listOf(
                CategoryEntity(name = "餐饮", icon = "meal", color = "#FF5C7C", type = 0, isDefault = true),
                CategoryEntity(name = "零食", icon = "coffee", color = "#FFB3C6", type = 0, isDefault = true),
                CategoryEntity(name = "超市", icon = "grocery", color = "#FF85A2", type = 0, isDefault = true),
                CategoryEntity(name = "交通", icon = "car", color = "#FFB7C5", type = 0, isDefault = true),
                CategoryEntity(name = "打车", icon = "taxi", color = "#FFC4D6", type = 0, isDefault = true),
                CategoryEntity(name = "购物", icon = "shopping", color = "#FF3366", type = 0, isDefault = true),
                CategoryEntity(name = "娱乐", icon = "game", color = "#FF6B8A", type = 0, isDefault = true),
                CategoryEntity(name = "电影", icon = "movie", color = "#E91E63", type = 0, isDefault = true),
                CategoryEntity(name = "服装", icon = "clothes", color = "#FF8FAB", type = 0, isDefault = true),
                CategoryEntity(name = "美妆", icon = "beauty", color = "#F06292", type = 0, isDefault = true),
                CategoryEntity(name = "通讯", icon = "phone", color = "#CE93D8", type = 0, isDefault = true),
                CategoryEntity(name = "医疗", icon = "medical", color = "#FFCDD2", type = 0, isDefault = true),
                CategoryEntity(name = "教育", icon = "school", color = "#BA68C8", type = 0, isDefault = true),
                CategoryEntity(name = "健身", icon = "fitness", color = "#F48FB1", type = 0, isDefault = true),
                CategoryEntity(name = "家居", icon = "home", color = "#FF80AB", type = 0, isDefault = true),
                CategoryEntity(name = "礼物", icon = "gift", color = "#FF5252", type = 0, isDefault = true),
                CategoryEntity(name = "其他", icon = "other", color = "#FFA8B8", type = 0, isDefault = true)
            )
            val incomeCategories = listOf(
                CategoryEntity(name = "工资", icon = "salary", color = "#FF5C7C", type = 1, isDefault = true),
                CategoryEntity(name = "奖金", icon = "bonus", color = "#FF3366", type = 1, isDefault = true),
                CategoryEntity(name = "投资收益", icon = "investment", color = "#E91E63", type = 1, isDefault = true),
                CategoryEntity(name = "其他", icon = "other_income", color = "#FFB7C5", type = 1, isDefault = true)
            )
            categoryDao.insertCategories(expenseCategories + incomeCategories)
        }
    }
}