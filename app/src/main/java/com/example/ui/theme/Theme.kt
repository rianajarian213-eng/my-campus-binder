package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BinderEspresso,
    onPrimary = Color.White,
    primaryContainer = BinderPaperSand,
    onPrimaryContainer = BinderEspresso,
    secondary = BinderWarmCaramel,
    onSecondary = Color.White,
    secondaryContainer = BinderPaperCard,
    onSecondaryContainer = BinderEspresso,
    tertiary = PastelTerracotta,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFBECE9),
    onTertiaryContainer = Color(0xFF4A1A12),
    background = BinderCreamBg,
    onBackground = BinderTextPrimary,
    surface = BinderPaperWhite,
    onSurface = BinderTextPrimary,
    surfaceVariant = BinderPaperCard,
    onSurfaceVariant = BinderTextSecondary,
    outline = BinderPaperSand,
    outlineVariant = BinderRuledLine
)

private val DarkColorScheme = darkColorScheme(
    primary = BinderWarmCaramel,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF38291F),
    onPrimaryContainer = Color(0xFFEADFD5),
    secondary = BinderGoldRing,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF43362A),
    onSecondaryContainer = Color(0xFFF5EEDB),
    tertiary = PastelSageGreen,
    onTertiary = Color.White,
    background = Color(0xFF1E1A17),
    onBackground = Color(0xFFEDE4DC),
    surface = Color(0xFF26211D),
    onSurface = Color(0xFFEDE4DC),
    surfaceVariant = Color(0xFF322C26),
    onSurfaceVariant = Color(0xFFD4C8BC)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep intentional stationery theme aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
