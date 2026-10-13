package com.example.studentsdaily.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme = lightColorScheme(
    primary = FinancialPrimary,
    onPrimary = FinancialOnPrimary,
    primaryContainer = FinancialPrimaryContainer,
    onPrimaryContainer = FinancialOnPrimaryContainer,
    secondary = FinancialSecondary,
    onSecondary = FinancialOnSecondary,
    secondaryContainer = FinancialSecondaryContainer,
    onSecondaryContainer = FinancialOnSecondaryContainer,
    background = FinancialBackground,
    onBackground = FinancialOnBackground,
    surface = FinancialSurface,
    onSurface = FinancialOnSurface,
    surfaceVariant = FinancialSurfaceVariant,
    onSurfaceVariant = FinancialOnSurfaceVariant,
    outline = FinancialOutline
)

private val DarkColorScheme = darkColorScheme(
    primary = FinancialPrimaryDark,
    onPrimary = FinancialOnPrimaryDark,
    primaryContainer = FinancialPrimaryContainerDark,
    onPrimaryContainer = FinancialOnPrimaryContainerDark,
    secondary = FinancialSecondaryDark,
    onSecondary = FinancialOnSecondaryDark,
    secondaryContainer = FinancialSecondaryContainerDark,
    onSecondaryContainer = FinancialOnSecondaryContainerDark,
    background = FinancialBackgroundDark,
    onBackground = FinancialOnBackgroundDark,
    surface = FinancialSurfaceDark,
    onSurface = FinancialOnSurfaceDark
)

@Composable
fun StudentsDailyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Set dynamicColor = false by default to enforce the custom light-green financial theme
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
