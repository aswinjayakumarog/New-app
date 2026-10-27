package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = CineAmber,
  onPrimary = CinemaMidnight,
  primaryContainer = CineAmberDark,
  onPrimaryContainer = Color.White,
  secondary = CineCyan,
  onSecondary = CinemaMidnight,
  secondaryContainer = Color(0xFF0369A1),
  onSecondaryContainer = Color.White,
  tertiary = CineEmerald,
  onTertiary = CinemaMidnight,
  background = CinemaMidnight,
  onBackground = TextPrimary,
  surface = CinemaSurfaceDark,
  onSurface = TextPrimary,
  surfaceVariant = CinemaSurfaceCard,
  onSurfaceVariant = TextSecondary,
  outline = CardBorder
)

private val LightColorScheme = lightColorScheme(
  primary = Color(0xFFD97706),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFFEF3C7),
  onPrimaryContainer = Color(0xFF78350F),
  secondary = Color(0xFF0284C7),
  onSecondary = Color.White,
  tertiary = Color(0xFF059669),
  onTertiary = Color.White,
  background = Color(0xFFF8FAFC),
  onBackground = Color(0xFF0F172A),
  surface = Color.White,
  onSurface = Color(0xFF0F172A),
  surfaceVariant = Color(0xFFF1F5F9),
  onSurfaceVariant = Color(0xFF475569),
  outline = Color(0xFFE2E8F0)
)

@Composable
fun CineTrackTheme(
  darkTheme: Boolean = true, // Default to cinema dark theme for immersive viewing
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
