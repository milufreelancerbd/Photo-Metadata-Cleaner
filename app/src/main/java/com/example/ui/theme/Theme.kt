package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CleanerPalette(
  val paper: Color,
  val panel: Color,
  val panelSubtle: Color,
  val ink: Color,
  val inkSoft: Color,
  val rule: Color,
  val ruleStrong: Color,
  val accent: Color,
  val accentSoft: Color,
  val accentText: Color,
  val success: Color,
  val successSoft: Color,
  val successText: Color,
)

private val LightPalette =
  CleanerPalette(
    paper = PaperLight,
    panel = PanelLight,
    panelSubtle = PanelSubtleLight,
    ink = InkLight,
    inkSoft = InkSoftLight,
    rule = RuleLight,
    ruleStrong = RuleStrongLight,
    accent = AccentLight,
    accentSoft = AccentSoftLight,
    accentText = AccentTextLight,
    success = SuccessLight,
    successSoft = SuccessSoftLight,
    successText = SuccessTextLight,
  )

private val DarkPalette =
  CleanerPalette(
    paper = PaperDark,
    panel = PanelDark,
    panelSubtle = PanelSubtleDark,
    ink = InkDark,
    inkSoft = InkSoftDark,
    rule = RuleDark,
    ruleStrong = RuleStrongDark,
    accent = AccentDark,
    accentSoft = AccentSoftDark,
    accentText = AccentTextDark,
    success = SuccessDark,
    successSoft = SuccessSoftDark,
    successText = SuccessTextDark,
  )

val LocalCleanerPalette = staticCompositionLocalOf { LightPalette }

private val LightColorScheme =
  lightColorScheme(
    primary = AccentLight,
    onPrimary = Color.White,
    secondary = SuccessLight,
    onSecondary = Color.White,
    background = PaperLight,
    onBackground = InkLight,
    surface = PanelLight,
    onSurface = InkLight,
    surfaceVariant = PanelSubtleLight,
    onSurfaceVariant = InkSoftLight,
    outline = RuleStrongLight,
    outlineVariant = RuleLight,
    error = AccentLight,
    onError = Color.White,
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = AccentDark,
    onPrimary = Color.White,
    secondary = SuccessDark,
    onSecondary = Color.White,
    background = PaperDark,
    onBackground = InkDark,
    surface = PanelDark,
    onSurface = InkDark,
    surfaceVariant = PanelSubtleDark,
    onSurfaceVariant = InkSoftDark,
    outline = RuleStrongDark,
    outlineVariant = RuleDark,
    error = AccentDark,
    onError = Color.White,
  )

@Composable
fun PhotoCleanerTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val palette = if (darkTheme) DarkPalette else LightPalette
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  CompositionLocalProvider(LocalCleanerPalette provides palette) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}
