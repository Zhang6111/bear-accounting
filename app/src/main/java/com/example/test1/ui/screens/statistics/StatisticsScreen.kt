package com.example.test1.ui.screens.statistics

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.test1.domain.model.Category
import com.example.test1.domain.model.Transaction
import com.example.test1.domain.model.TransactionType
import com.example.test1.ui.components.CategoryIcons
import com.example.test1.ui.theme.ExpenseRed
import com.example.test1.ui.theme.IncomeGreen

enum class TimeRange(val label: String) {
    TODAY("今天"),
    WEEK("本周"),
    MONTH("本月"),
    YEAR("本年"),
    ALL("全部")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: () -> Unit,
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }

    LaunchedEffect(uiState.exportFile) {
        uiState.exportFile?.let { file ->
            val shareIntent = viewModel.getShareIntent(file)
            context.startActivity(Intent.createChooser(shareIntent, "导出记账数据"))
            viewModel.clearExportFile()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("小熊统计", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.exportData() }) {
                        Icon(Icons.Default.Share, contentDescription = "导出")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            TimeRangeSelector(
                selectedRange = uiState.selectedTimeRange,
                onRangeSelected = { viewModel.selectTimeRange(it) }
            )

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        OverviewCard(income = uiState.totalIncome, expense = uiState.totalExpense)
                    }

                    if (uiState.expenseByCategory.isNotEmpty()) {
                        item {
                            CategoryChartCard(title = "支出分类", data = uiState.expenseByCategory, categories = uiState.categories, total = uiState.totalExpense)
                        }
                    }

                    if (uiState.incomeByCategory.isNotEmpty()) {
                        item {
                            CategoryChartCard(title = "收入分类", data = uiState.incomeByCategory, categories = uiState.categories, total = uiState.totalIncome)
                        }
                    }

                    item {
                        Text("交易明细", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }

                    items(uiState.transactions, key = { it.id }) { transaction ->
                        StatisticsTransactionItem(transaction = transaction, category = uiState.categories.find { it.id == transaction.categoryId })
                    }

                    if (uiState.transactions.isEmpty()) {
                        item { StatisticsEmptyState() }
                    }
                }
            }
        }
    }
}

@Composable
fun TimeRangeSelector(selectedRange: TimeRange, onRangeSelected: (TimeRange) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(TimeRange.entries.toTypedArray()) { range ->
            FilterChip(
                selected = selectedRange == range,
                onClick = { onRangeSelected(range) },
                label = { Text(range.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun OverviewCard(income: Double, expense: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.TrendingUp, contentDescription = null, tint = IncomeGreen, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text("收入", style = MaterialTheme.typography.labelMedium)
                Text("¥ ${String.format("%.2f", income)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = IncomeGreen)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.TrendingDown, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(28.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text("支出", style = MaterialTheme.typography.labelMedium)
                Text("¥ ${String.format("%.2f", expense)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = ExpenseRed)
            }
        }
    }
}

@Composable
fun CategoryChartCard(title: String, data: List<Pair<Long, Double>>, categories: List<Category>, total: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            data.sortedByDescending { it.second }.take(5).forEach { (categoryId, amount) ->
                val category = categories.find { it.id == categoryId }
                val percentage = if (total > 0) (amount / total * 100) else 0.0
                StatisticsCategoryProgressItem(category = category, amount = amount, percentage = percentage)
            }
        }
    }
}

@Composable
fun StatisticsCategoryProgressItem(category: Category?, amount: Double, percentage: Double) {
    val defaultColor = MaterialTheme.colorScheme.primary
    val categoryColor = remember(category?.color, defaultColor) {
        try { Color(android.graphics.Color.parseColor(category?.color ?: "#FFB3C6")) } 
        catch (e: Exception) { defaultColor }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(28.dp).clip(CircleShape).background(categoryColor.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Icon(CategoryIcons.getIcon(category?.icon ?: "other"), contentDescription = null, tint = categoryColor, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(category?.name ?: "未知", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("${String.format("%.1f", percentage)}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Text("¥ ${String.format("%.2f", amount)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (percentage / 100).toFloat().coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
            color = categoryColor,
            trackColor = categoryColor.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun StatisticsTransactionItem(transaction: Transaction, category: Category?) {
    val defaultColor = MaterialTheme.colorScheme.primaryContainer
    val categoryColor = remember(category?.color, defaultColor) {
        try { Color(android.graphics.Color.parseColor(category?.color ?: "#FFB3C6")) } 
        catch (e: Exception) { defaultColor }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(categoryColor.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Icon(CategoryIcons.getIcon(category?.icon ?: "other"), contentDescription = null, tint = categoryColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(category?.name ?: "未知", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                if (transaction.note.isNotEmpty()) {
                    Text(transaction.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Text("${if (transaction.type == TransactionType.INCOME) "+" else "-"}¥${String.format("%.2f", transaction.amount)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = if (transaction.type == TransactionType.INCOME) IncomeGreen else ExpenseRed)
        }
    }
}

@Composable
fun StatisticsEmptyState() {
    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text("暂无数据", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
        }
    }
}