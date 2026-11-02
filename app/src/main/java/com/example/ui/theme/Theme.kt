package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RolexDarkColorScheme = darkColorScheme(
    primary = RolexGold,
    onPrimary = OnyxBlack,
    primaryContainer = RolexGoldDark,
    onPrimaryContainer = Color.White,
    secondary = RolexGreenLight,
    onSecondary = OnyxBlack,
    secondaryContainer = RolexGreen,
    onSecondaryContainer = Color.White,
    tertiary = RolexGoldLight,
    onTertiary = OnyxBlack,
    background = OnyxBlack,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    outlineVariant = GoldBorder,
    error = StatusExpired,
    onError = Color.White
)

@Composable
fun RolexFastAutoTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RolexDarkColorScheme,
        typography = Typography,
        content = content
    )
}
