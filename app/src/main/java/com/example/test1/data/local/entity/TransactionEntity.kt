package com.example.test1.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val type: Int, // 0: expense, 1: income
    val categoryId: Long,
    val note: String,
    val date: Long, // timestamp
    val createdAt: Long = System.currentTimeMillis()
)