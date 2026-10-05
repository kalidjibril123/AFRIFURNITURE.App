package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AfriRoyalBlue,
    secondary = AfriTeal,
    tertiary = AfriGold,
    background = AfriDarkBg,
    surface = AfriDarkSurface,
    onPrimary = AfriLightSurface,
    onSecondary = AfriLightSurface,
    onBackground = AfriLightBg,
    onSurface = AfriLightBg
)

private val LightColorScheme = lightColorScheme(
    primary = AfriDeepBlue,
    secondary = AfriRoyalBlue,
    tertiary = AfriTeal,
    background = AfriLightBg,
    surface = AfriLightSurface,
    onPrimary = AfriLightSurface,
    onSecondary = AfriLightSurface,
    onBackground = AfriDarkBg,
    onSurface = AfriDarkBg
)

@Composable
fun MyApplicationTheme(
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
