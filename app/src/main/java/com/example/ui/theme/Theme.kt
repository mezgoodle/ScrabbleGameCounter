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

private val ScrabbleBoardColorScheme = darkColorScheme(
    primary = WoodTileGold,
    onPrimary = WoodTileText,
    secondary = ActiveFeltGreen,
    onSecondary = Color.White,
    background = BoardDarkBg,
    onBackground = WarmOffWhite,
    surface = BoardDarkSurface,
    onSurface = WarmOffWhite,
    surfaceVariant = BoardDarkSurfaceVariant,
    onSurfaceVariant = SageMuted,
    error = DangerRed,
    onError = Color.Black
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force premium board-game dark style by default or allow system
    content: @Composable () -> Unit,
) {
    // We strictly use our custom premium Scrabble Board Color Scheme to give safety,
    // beauty, and uniform branding across all system versions.
    MaterialTheme(
        colorScheme = ScrabbleBoardColorScheme,
        typography = Typography,
        content = content
    )
}
