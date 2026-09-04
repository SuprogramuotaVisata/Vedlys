package com.suprogramuota_visata.vedlys.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = DeepCosmicBlue,
    primaryContainer = SpaceLight,
    onPrimaryContainer = CyberCyan,
    secondary = LazerOrange,
    onSecondary = DeepCosmicBlue,
    tertiary = DataGreen,
    background = SpaceDarker, // Darker background
    surface = SpaceLight,
    onBackground = CloudWhite,
    onSurface = CloudWhite,
    error = ErrorRed,
    surfaceVariant = Color(0xFF1E1E1E) // Distinct input background
)

private val LightColorScheme = lightColorScheme(
    primary = DeepCosmicBlue,
    onPrimary = CloudWhite,
    primaryContainer = CyberCyanDark,
    onPrimaryContainer = CloudWhite,
    secondary = LazerOrangeDark,
    onSecondary = CloudWhite,
    tertiary = AnalyticsPurpleDark,
    background = Stone50,
    surface = Color.White,
    onBackground = Stone900, // Very dark text for contrast
    onSurface = Stone900,
    error = ErrorRed,
    surfaceVariant = Stone100 // Input background
)

@Composable
fun VedlysTheme(
    darkTheme: Boolean = true,
    fontSize: AppFontSize = AppFontSize.Medium,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = getTypography(fontSize),
        content = content
    )
}
