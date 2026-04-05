package com.example.test1.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

object CategoryIcons {
    val Meal: ImageVector = Icons.Filled.Restaurant
    val Coffee: ImageVector = Icons.Filled.LocalCafe
    val Grocery: ImageVector = Icons.Filled.LocalGroceryStore
    val Car: ImageVector = Icons.Filled.DirectionsCar
    val Taxi: ImageVector = Icons.Filled.LocalTaxi
    val Gas: ImageVector = Icons.Filled.LocalGasStation
    val Flight: ImageVector = Icons.Filled.Flight
    val Shopping: ImageVector = Icons.Filled.ShoppingBag
    val Cart: ImageVector = Icons.Filled.ShoppingCart
    val Game: ImageVector = Icons.Filled.SportsEsports
    val Movie: ImageVector = Icons.Filled.Movie
    val Music: ImageVector = Icons.Filled.MusicNote
    val Clothes: ImageVector = Icons.Filled.Checkroom
    val Beauty: ImageVector = Icons.Filled.Face
    val Phone: ImageVector = Icons.Filled.Smartphone
    val Medical: ImageVector = Icons.Filled.LocalHospital
    val School: ImageVector = Icons.Filled.School
    val Fitness: ImageVector = Icons.Filled.FitnessCenter
    val Home: ImageVector = Icons.Filled.Home
    val Gift: ImageVector = Icons.Filled.CardGiftcard
    val Other: ImageVector = Icons.Filled.MoreHoriz
    val Salary: ImageVector = Icons.Filled.AccountBalance
    val Bonus: ImageVector = Icons.Filled.Star
    val Investment: ImageVector = Icons.Filled.TrendingUp
    val OtherIncome: ImageVector = Icons.Filled.Savings
    
    fun getIcon(iconName: String): ImageVector {
        return when (iconName) {
            "meal" -> Meal
            "coffee" -> Coffee
            "grocery" -> Grocery
            "car" -> Car
            "taxi" -> Taxi
            "gas" -> Gas
            "flight" -> Flight
            "shopping" -> Shopping
            "cart" -> Cart
            "game" -> Game
            "movie" -> Movie
            "music" -> Music
            "clothes" -> Clothes
            "beauty" -> Beauty
            "phone" -> Phone
            "medical" -> Medical
            "school" -> School
            "fitness" -> Fitness
            "home" -> Home
            "gift" -> Gift
            "other" -> Other
            "salary" -> Salary
            "bonus" -> Bonus
            "investment" -> Investment
            "other_income" -> OtherIncome
            else -> Other
        }
    }
}