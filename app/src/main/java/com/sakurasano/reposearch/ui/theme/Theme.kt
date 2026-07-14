package com.sakurasano.reposearch.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BlueLight,
    onPrimary = Color.White,
    primaryContainer = BlueContainerLight,
    onPrimaryContainer = OnBlueContainerLight,
    secondary = GreenLight,
    onSecondary = Color.White,
    secondaryContainer = GreenContainerLight,
    onSecondaryContainer = OnGreenContainerLight,
    tertiary = PurpleLight,
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = OnSurfaceLight,
    surface = BackgroundLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceSubtleLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = SurfaceSubtleLight,
    surfaceContainer = SurfaceSubtleLight,
    surfaceContainerHigh = SurfaceMutedLight,
    surfaceContainerHighest = SurfaceMutedLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    error = RedLight,
    onError = Color.White,
    errorContainer = RedContainerLight,
    onErrorContainer = OnRedContainerLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = BlueDark,
    onPrimary = BackgroundDark,
    primaryContainer = BlueContainerDark,
    onPrimaryContainer = OnBlueContainerDark,
    secondary = GreenDark,
    onSecondary = BackgroundDark,
    secondaryContainer = GreenContainerDark,
    onSecondaryContainer = OnGreenContainerDark,
    tertiary = PurpleDark,
    onTertiary = BackgroundDark,
    background = BackgroundDark,
    onBackground = OnSurfaceDark,
    surface = BackgroundDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceContainerHighDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = BackgroundDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    error = RedDark,
    onError = BackgroundDark,
    errorContainer = RedContainerDark,
    onErrorContainer = OnRedContainerDark,
)

@Composable
fun GitHubRepoSearchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content,
    )
}
