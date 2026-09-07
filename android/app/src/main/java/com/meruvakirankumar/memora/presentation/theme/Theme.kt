package com.meruvakirankumar.memora.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFFD84F2A),
    secondary = Color(0xFF17312D),
    background = Color(0xFFF6F2E8),
    surface = Color(0xFFFFFDF7),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF3BE4E),
    secondary = Color(0xFFD8E8DE),
    background = Color(0xFF0E2A2B),
    surface = Color(0xFF16201D),
)

@Composable
fun MemoraTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColors else LightColors,
        content = content,
    )
}
