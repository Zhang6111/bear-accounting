package com.example.test1.ui.screens.category

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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.test1.domain.model.TransactionType
import com.example.test1.ui.theme.ExpenseRed
import com.example.test1.ui.theme.IncomeGreen
import com.example.test1.ui.theme.Pink100

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCategoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: CategoryEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) { onNavigateBack() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Pets, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("添加分类", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(scrollState).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CategoryTypeSelector(selectedType = uiState.type, onTypeSelected = { viewModel.updateType(it) })
            CategoryNameInput(name = uiState.name, onNameChange = { viewModel.updateName(it) })
            CategoryIconSelector(selectedIcon = uiState.icon, onIconSelected = { viewModel.updateIcon(it) })
            CategoryColorSelector(selectedColor = uiState.color, onColorSelected = { viewModel.updateColor(it) })

            Spacer(modifier = Modifier.height(8.dp))

            CategorySaveButton(enabled = uiState.name.isNotEmpty(), onClick = { viewModel.saveCategory() })
        }
    }
}

@Composable
fun CategoryTypeSelector(selectedType: TransactionType, onTypeSelected: (TransactionType) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TransactionType.entries.forEach { type ->
            val isSelected = selectedType == type
            val backgroundColor = if (isSelected) {
                if (type == TransactionType.EXPENSE) ExpenseRed.copy(alpha = 0.15f) else IncomeGreen.copy(alpha = 0.15f)
            } else { MaterialTheme.colorScheme.surfaceVariant }
            val contentColor = if (isSelected) {
                if (type == TransactionType.EXPENSE) ExpenseRed else IncomeGreen
            } else { MaterialTheme.colorScheme.onSurfaceVariant }

            Row(
                modifier = Modifier.weight(1f).clip(RoundedCornerShape(16.dp)).background(backgroundColor).clickable { onTypeSelected(type) }.padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    if (type == TransactionType.EXPENSE) Icons.Default.TrendingDown else Icons.Default.TrendingUp,
                    contentDescription = null, tint = contentColor, modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (type == TransactionType.EXPENSE) "支出" else "收入", fontWeight = FontWeight.Bold, color = contentColor)
            }
        }
    }
}

@Composable
fun CategoryNameInput(name: String, onNameChange: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(12.dp))
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("分类名称") },
                placeholder = { Text("请输入分类名称") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}

@Composable
fun CategoryIconSelector(selectedIcon: String, onIconSelected: (String) -> Unit) {
    val icons = listOf(
        "meal" to Icons.Default.Restaurant,
        "coffee" to Icons.Default.LocalCafe,
        "grocery" to Icons.Default.LocalGroceryStore,
        "car" to Icons.Default.DirectionsCar,
        "taxi" to Icons.Default.LocalTaxi,
        "shopping" to Icons.Default.ShoppingBag,
        "cart" to Icons.Default.ShoppingCart,
        "game" to Icons.Default.SportsEsports,
        "movie" to Icons.Default.Movie,
        "clothes" to Icons.Default.Checkroom,
        "beauty" to Icons.Default.Face,
        "phone" to Icons.Default.Smartphone,
        "medical" to Icons.Default.LocalHospital,
        "school" to Icons.Default.School,
        "fitness" to Icons.Default.FitnessCenter,
        "home" to Icons.Default.Home,
        "gift" to Icons.Default.CardGiftcard,
        "other" to Icons.Default.MoreHoriz,
        "salary" to Icons.Default.AccountBalance,
        "bonus" to Icons.Default.Star,
        "investment" to Icons.Default.TrendingUp,
        "other_income" to Icons.Default.Savings
    )
    val defaultColor = try { Color(android.graphics.Color.parseColor("#FFB3C6")) } catch (e: Exception) { Pink100 }

    Column {
        Text("选择图标", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.height(200.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(icons) { (name, icon) ->
                val isSelected = selectedIcon == name
                Box(
                    modifier = Modifier.aspectRatio(1f).clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) defaultColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
                        .border(if (isSelected) 2.dp else 0.dp, if (isSelected) defaultColor else Color.Transparent, RoundedCornerShape(12.dp))
                        .clickable { onIconSelected(name) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = name, tint = if (isSelected) defaultColor else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(26.dp))
                }
            }
        }
    }
}

@Composable
fun CategoryColorSelector(selectedColor: String, onColorSelected: (String) -> Unit) {
    val colors = listOf("#FF3366", "#FF5C7C", "#FF85A2", "#FFA8B8", "#FFB3C6", "#FFB7C5", "#FFCDD2", "#F48FB1", "#F06292", "#E91E63", "#CE93D8", "#BA68C8")

    Column {
        Text("选择颜色", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(6), modifier = Modifier.height(80.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(colors) { color ->
                val colorValue = Color(android.graphics.Color.parseColor(color))
                val isSelected = selectedColor == color
                Box(
                    modifier = Modifier.aspectRatio(1f).clip(CircleShape).background(colorValue)
                        .border(if (isSelected) 3.dp else 0.dp, if (isSelected) Color.White else Color.Transparent, CircleShape)
                        .clickable { onColorSelected(color) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun CategorySaveButton(enabled: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
        enabled = enabled,
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Icon(Icons.Default.Check, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("保存", fontWeight = FontWeight.Bold, fontSize = MaterialTheme.typography.titleMedium.fontSize)
    }
}