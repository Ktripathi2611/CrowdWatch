package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CrowdWatchColors.DarkPrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1E3A8A),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = CrowdWatchColors.NetworkPacket,
    onSecondary = Color.Black,
    background = CrowdWatchColors.DarkBackground,
    surface = CrowdWatchColors.DarkSurface,
    onBackground = CrowdWatchColors.DarkTextPrimary,
    onSurface = CrowdWatchColors.DarkTextPrimary,
    surfaceVariant = CrowdWatchColors.DarkSurfaceSecondary,
    onSurfaceVariant = CrowdWatchColors.DarkTextSecondary,
    outline = CrowdWatchColors.DarkBorder
)

private val LightColorScheme = lightColorScheme(
    primary = CrowdWatchColors.PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = CrowdWatchColors.PrimaryBlueBg,
    onPrimaryContainer = CrowdWatchColors.PrimaryBlueDark,
    secondary = CrowdWatchColors.Transport,
    onSecondary = Color.White,
    background = CrowdWatchColors.Background,
    surface = CrowdWatchColors.Surface,
    onBackground = CrowdWatchColors.TextPrimary,
    onSurface = CrowdWatchColors.TextPrimary,
    surfaceVariant = CrowdWatchColors.SurfaceSecondary,
    onSurfaceVariant = CrowdWatchColors.TextSecondary,
    outline = CrowdWatchColors.Border
)

@Composable
fun CrowdWatchTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
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

