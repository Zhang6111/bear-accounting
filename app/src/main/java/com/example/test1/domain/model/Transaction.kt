package com.example.test1.domain.model

data class Transaction(
    val id: Long = 0,
    val amount: Double,
    val type: TransactionType,
    val categoryId: Long,
    val note: String,
    val date: Long,
    val createdAt: Long = System.currentTimeMillis()
)

enum class TransactionType(val value: Int) {
    EXPENSE(0),
    INCOME(1);

    companion object {
        fun fromValue(value: Int): TransactionType {
            return entries.find { it.value == value } ?: EXPENSE
        }
    }
}