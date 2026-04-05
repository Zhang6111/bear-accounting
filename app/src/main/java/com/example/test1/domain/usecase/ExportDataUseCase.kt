package com.example.test1.domain.usecase

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.test1.domain.model.Category
import com.example.test1.domain.model.Transaction
import com.example.test1.domain.model.TransactionType
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class ExportDataUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun exportToCSV(
        transactions: List<Transaction>,
        categories: List<Category>
    ): File {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val fileNameFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        
        val fileName = "accounting_${fileNameFormat.format(Date())}.csv"
        val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        
        FileWriter(file).use { writer ->
            // 写入CSV标题
            writer.append("日期,类型,分类,金额,备注\n")
            
            // 写入数据
            transactions.forEach { transaction ->
                val category = categories.find { it.id == transaction.categoryId }
                val typeStr = if (transaction.type == TransactionType.INCOME) "收入" else "支出"
                val categoryName = category?.name ?: "未知"
                val amount = if (transaction.type == TransactionType.INCOME) {
                    transaction.amount
                } else {
                    -transaction.amount
                }
                val note = transaction.note.replace(",", " ").replace("\n", " ")
                
                writer.append("${dateFormat.format(Date(transaction.date))},$typeStr,$categoryName,$amount,$note\n")
            }
        }
        
        return file
    }

    fun shareFile(file: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}