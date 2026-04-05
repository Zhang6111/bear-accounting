package com.example.test1.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object AddTransaction : Screen("add_transaction")
    data object EditTransaction : Screen("edit_transaction/{transactionId}") {
        fun createRoute(transactionId: Long) = "edit_transaction/$transactionId"
    }
    data object Statistics : Screen("statistics")
    data object Category : Screen("category")
    data object AddCategory : Screen("add_category")
    data object EditCategory : Screen("edit_category/{categoryId}") {
        fun createRoute(categoryId: Long) = "edit_category/$categoryId"
    }
    data object Settings : Screen("settings")
    data object Feedback : Screen("feedback")
    data object Update : Screen("update")
}

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Home : BottomNavItem(
        route = Screen.Home.route,
        title = "首页",
        selectedIcon = Icons.Filled.Pets,
        unselectedIcon = Icons.Outlined.Pets
    )
    data object Statistics : BottomNavItem(
        route = Screen.Statistics.route,
        title = "统计",
        selectedIcon = Icons.Filled.PieChart,
        unselectedIcon = Icons.Outlined.PieChart
    )
    data object Add : BottomNavItem(
        route = Screen.AddTransaction.route,
        title = "记账",
        selectedIcon = Icons.Filled.AddCircle,
        unselectedIcon = Icons.Outlined.AddCircle
    )
    data object Category : BottomNavItem(
        route = Screen.Category.route,
        title = "分类",
        selectedIcon = Icons.Filled.GridView,
        unselectedIcon = Icons.Outlined.GridView
    )
    data object Settings : BottomNavItem(
        route = Screen.Settings.route,
        title = "设置",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Home,
    BottomNavItem.Statistics,
    BottomNavItem.Add,
    BottomNavItem.Settings
)