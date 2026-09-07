package com.meruvakirankumar.memora.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF166C5B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFA6F2DD),
    onPrimaryContainer = Color(0xFF00201A),
    secondary = Color(0xFF4B635B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFCDE8DE),
    onSecondaryContainer = Color(0xFF071F19),
    tertiary = Color(0xFFA15A2B),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDCC2),
    onTertiaryContainer = Color(0xFF351000),
    background = Color(0xFFF6F8F7),
    onBackground = Color(0xFF191C1B),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF191C1B),
    surfaceVariant = Color(0xFFDBE5DF),
    onSurfaceVariant = Color(0xFF3F4945),
    outline = Color(0xFF6F7975),
    outlineVariant = Color(0xFFBFC9C3),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8AD6C2),
    onPrimary = Color(0xFF00382E),
    primaryContainer = Color(0xFF005143),
    onPrimaryContainer = Color(0xFFA6F2DD),
    secondary = Color(0xFFB2CCC2),
    onSecondary = Color(0xFF1D352D),
    secondaryContainer = Color(0xFF334B43),
    onSecondaryContainer = Color(0xFFCDE8DE),
    tertiary = Color(0xFFFFB68A),
    onTertiary = Color(0xFF562000),
    background = Color(0xFF0F1513),
    onBackground = Color(0xFFE1E3E0),
    surface = Color(0xFF171D1B),
    onSurface = Color(0xFFE1E3E0),
    surfaceVariant = Color(0xFF3F4945),
    onSurfaceVariant = Color(0xFFBFC9C3),
    outline = Color(0xFF899390),
    outlineVariant = Color(0xFF3F4945),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
)

private val MemoraTypography = Typography().run {
    copy(
        headlineMedium = headlineMedium.copy(fontWeight = FontWeight.Bold, fontSize = 28.sp),
        headlineSmall = headlineSmall.copy(fontWeight = FontWeight.Bold),
        titleLarge = titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelLarge = labelLarge.copy(fontWeight = FontWeight.SemiBold),
    )
}

private val MemoraShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun MemoraTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColors else LightColors,
        typography = MemoraTypography,
        shapes = MemoraShapes,
        content = content,
    )
}

/** Semantic colors for memory status, adapting to light/dark. */
object StatusColors {
    @Composable
    fun overdueContainer(): Color = MaterialTheme.colorScheme.errorContainer
    @Composable
    fun overdueContent(): Color = MaterialTheme.colorScheme.onErrorContainer
    @Composable
    fun dueTodayContainer(): Color = MaterialTheme.colorScheme.tertiaryContainer
    @Composable
    fun dueTodayContent(): Color = MaterialTheme.colorScheme.onTertiaryContainer
    @Composable
    fun upcomingContainer(): Color = MaterialTheme.colorScheme.secondaryContainer
    @Composable
    fun upcomingContent(): Color = MaterialTheme.colorScheme.onSecondaryContainer
    @Composable
    fun completedContainer(): Color = MaterialTheme.colorScheme.surfaceVariant
    @Composable
    fun completedContent(): Color = MaterialTheme.colorScheme.onSurfaceVariant
}
