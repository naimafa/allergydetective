package com.nfar.allergydetective.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Palette from UX_UI_Prototyp 1.1 (dark variants for text). The final design follows in phase 3.
private val PetrolText = Color(0xFF2E6371)
private val SageText = Color(0xFF4F6F67)
private val CoralText = Color(0xFFB5453F)

private val LightColors = lightColorScheme(
    primary = PetrolText,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD6EBEF),
    onPrimaryContainer = Color(0xFF0F2E35),
    secondary = SageText,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFDDE9E5),
    onSecondaryContainer = Color(0xFF1C2E29),
    tertiary = CoralText,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFDAD8),
    onTertiaryContainer = Color(0xFF410004),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8FCBDA),
    onPrimary = Color(0xFF00363F),
    primaryContainer = PetrolText,
    onPrimaryContainer = Color(0xFFD6EBEF),
    secondary = Color(0xFFB3CEC6),
    onSecondary = Color(0xFF1C2E29),
    secondaryContainer = SageText,
    onSecondaryContainer = Color(0xFFDDE9E5),
    tertiary = Color(0xFFFFB3AF),
    onTertiary = Color(0xFF68000B),
    tertiaryContainer = CoralText,
    onTertiaryContainer = Color(0xFFFFDAD8),
)

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors, content = content)
}
