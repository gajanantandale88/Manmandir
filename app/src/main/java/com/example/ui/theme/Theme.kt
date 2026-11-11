package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// High-contrast, clean Light Color Scheme designed for maximum font clarity and readability
private val LightColorScheme =
  lightColorScheme(
    primary = SocietyNavy,
    onPrimary = Color.White,
    primaryContainer = SocietyGoldContainer,
    onPrimaryContainer = SocietyNavyDark,
    secondary = SocietySlate,
    onSecondary = Color.White,
    secondaryContainer = SocietySurfaceVariant,
    onSecondaryContainer = SocietyNavyDark,
    tertiary = SocietyGold,
    onTertiary = SocietyNavyDark,
    tertiaryContainer = SocietyGoldContainer,
    onTertiaryContainer = SocietyNavyDark,
    background = SocietyBg,
    onBackground = SocietyNavyDark,
    surface = SocietySurface,
    onSurface = SocietyNavyDark,
    surfaceVariant = SocietySurfaceVariant,
    onSurfaceVariant = SocietyNavy,
    outline = SocietyCardBorder,
    outlineVariant = Color(0xFFCBD5E1),
    error = SocietyError,
    errorContainer = SocietyErrorContainer,
    onError = Color.White,
    onErrorContainer = SocietyError
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false, // Strictly enforce light mode as requested; ignore system dark mode
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  // Always use crisp LightColorScheme for 100% visible fonts and high contrast
  MaterialTheme(
    colorScheme = LightColorScheme,
    typography = Typography,
    content = content
  )
}


