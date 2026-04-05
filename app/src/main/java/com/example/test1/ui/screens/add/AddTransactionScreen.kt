package com.example.test1.ui.screens.add

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.test1.domain.model.Category
import com.example.test1.domain.model.TransactionType
import com.example.test1.ui.components.CategoryIcons
import com.example.test1.ui.theme.ExpenseRed
import com.example.test1.ui.theme.IncomeGreen
import com.example.test1.ui.theme.Pink100

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    onNavigateBack: () -> Unit,
    viewModel: AddTransactionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) { viewModel.loadCategories() }
    LaunchedEffect(uiState.isSaved) { if (uiState.isSaved) onNavigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("小熊记账", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp)
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TransactionTypeRow(
                selectedType = uiState.type,
                onTypeSelected = { viewModel.updateType(it) }
            )

            AmountCard(
                amount = uiState.amount,
                onAmountChange = { viewModel.updateAmount(it) }
            )

            CategoryGrid(
                title = "选择分类",
                categories = uiState.categories,
                selectedCategoryId = uiState.selectedCategoryId,
                onCategorySelected = { viewModel.updateCategory(it) }
            )

            DateCard(
                selectedDate = uiState.date,
                onDateSelected = { viewModel.updateDate(it) }
            )

            NoteCard(
                note = uiState.note,
                onNoteChange = { viewModel.updateNote(it) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.saveTransaction() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.type == TransactionType.EXPENSE) ExpenseRed else IncomeGreen
                ),
                enabled = uiState.amount > 0 && uiState.selectedCategoryId != null
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("保存", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TransactionTypeRow(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TransactionType.entries.forEach { type ->
            val isSelected = selectedType == type
            val bgColor = if (isSelected) {
                if (type == TransactionType.EXPENSE) ExpenseRed.copy(alpha = 0.15f) else IncomeGreen.copy(alpha = 0.15f)
            } else MaterialTheme.colorScheme.surfaceVariant
            val contentColor = if (isSelected) {
                if (type == TransactionType.EXPENSE) ExpenseRed else IncomeGreen
            } else MaterialTheme.colorScheme.onSurfaceVariant

            Row(
                modifier = Modifier.weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .background(bgColor)
                    .clickable { onTypeSelected(type) }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (type == TransactionType.EXPENSE) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                    contentDescription = null, tint = contentColor, modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    if (type == TransactionType.EXPENSE) "支出" else "收入",
                    fontWeight = FontWeight.Bold, color = contentColor
                )
            }
        }
    }
}

@Composable
fun AmountCard(amount: Double, onAmountChange: (Double) -> Unit) {
    var inputText by remember(amount) { mutableStateOf(if (amount > 0) amount.toString() else "") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("金额", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("¥", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it; it.toDoubleOrNull()?.let { v -> onAmountChange(v) } },
                    modifier = Modifier.width(160.dp),
                    textStyle = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color.Transparent, focusedBorderColor = Color.Transparent),
                    placeholder = { Text("0.00", style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f), modifier = Modifier.fillMaxWidth()) }
                )
            }
        }
    }
}

@Composable
fun CategoryGrid(title: String, categories: List<Category>, selectedCategoryId: Long?, onCategorySelected: (Long) -> Unit) {
    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.height(180.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(categories, key = { it.id }) { category ->
                val isSelected = category.id == selectedCategoryId
                val categoryColor = remember(category.color) { try { Color(android.graphics.Color.parseColor(category.color)) } catch (e: Exception) { Pink100 } }
                
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (isSelected) categoryColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .then(if (isSelected) Modifier.border(2.dp, categoryColor, RoundedCornerShape(16.dp)) else Modifier)
                        .clickable { onCategorySelected(category.id) }
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(categoryColor.copy(alpha = 0.25f)), contentAlignment = Alignment.Center) {
                        Icon(CategoryIcons.getIcon(category.icon), contentDescription = null, tint = categoryColor, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(category.name, style = MaterialTheme.typography.labelMedium, color = if (isSelected) categoryColor else MaterialTheme.colorScheme.onSurface, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateCard(selectedDate: Long, onDateSelected: (Long) -> Unit) {
    var showPicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { showPicker = true },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("日期", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Text(formatDate(selectedDate), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), modifier = Modifier.size(20.dp))
        }
    }

    if (showPicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { onDateSelected(it) }; showPicker = false }) { Text("确定") } },
            dismissButton = { TextButton(onClick = { showPicker = false }) { Text("取消") } }
        ) { DatePicker(state) }
    }
}

@Composable
fun NoteCard(note: String, onNoteChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("备注", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(value = note, onValueChange = onNoteChange, modifier = Modifier.fillMaxWidth(), placeholder = { Text("添加备注...") }, maxLines = 2, shape = RoundedCornerShape(12.dp))
        }
    }
}

fun formatDate(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}