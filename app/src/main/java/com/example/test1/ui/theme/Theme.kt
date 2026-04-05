package com.example.test1.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Pink500,
    onPrimary = Color.White,
    primaryContainer = Pink100,
    onPrimaryContainer = Color(0xFF5C0825),
    secondary = Pink300,
    onSecondary = Color.White,
    secondaryContainer = Pink100,
    onSecondaryContainer = Color(0xFF5C0825),
    tertiary = Color(0xFF4CAF50),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFE8F5E9),
    onTertiaryContainer = Color(0xFF1B5E20),
    error = Pink400,
    onError = Color.White,
    errorContainer = Color(0xFFFFD4E0),
    onErrorContainer = Color(0xFF5C0825),
    background = Color(0xFFFFF5F8),
    onBackground = Color(0xFF3A0A1F),
    surface = Color.White,
    onSurface = Color(0xFF3A0A1F),
    surfaceVariant = Pink50,
    onSurfaceVariant = Color(0xFF7A4A5E),
    outline = Color(0xFFDBB4C8),
    outlineVariant = Pink100,
    inverseSurface = Color(0xFF3A0A1F),
    inverseOnSurface = Color(0xFFFFF0F5),
    inversePrimary = Pink200
)

private val DarkColorScheme = darkColorScheme(
    primary = Pink200,
    onPrimary = Color(0xFF5C1128),
    primaryContainer = Color(0xFF7D2940),
    onPrimaryContainer = Pink100,
    secondary = Pink300,
    onSecondary = Color(0xFF3D2040),
    secondaryContainer = Color(0xFF563858),
    onSecondaryContainer = Pink100,
    tertiary = Color(0xFF81C784),
    onTertiary = Color(0xFF003A06),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    background = Color(0xFF1A1520),
    onBackground = Color(0xFFF0DEE3),
    surface = Color(0xFF251829),
    onSurface = Color(0xFFF0DEE3),
    surfaceVariant = Color(0xFF4A3A44),
    onSurfaceVariant = Color(0xFFCAC0C9),
    outline = Color(0xFF948A95)
)

@Composable
fun AccountingAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}