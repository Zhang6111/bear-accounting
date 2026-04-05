package com.example.test1.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val icon: String, // emoji or icon name
    val color: String, // hex color
    val type: Int, // 0: expense, 1: income
    val isDefault: Boolean = false
)