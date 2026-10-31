package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val VSCodeColorScheme = darkColorScheme(
    primary = VSCodeAccent,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0E639C),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF4EC9B0),
    onSecondary = Color.Black,
    background = VSCodeBackground,
    onBackground = VSCodeTextPrimary,
    surface = VSCodeSidebar,
    onSurface = VSCodeTextPrimary,
    surfaceVariant = VSCodeActivityBar,
    onSurfaceVariant = VSCodeTextSecondary,
    outline = VSCodeBorder
)

@Composable
fun VSCodeAppTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = VSCodeColorScheme,
        typography = Typography,
        content = content
    )
}
