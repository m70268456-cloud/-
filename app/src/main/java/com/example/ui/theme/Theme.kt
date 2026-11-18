package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Immutable
data class MalabnaStatusColors(
  val goingText: Color,
  val goingBg: Color,
  val notGoingText: Color,
  val notGoingBg: Color,
  val pendingText: Color,
  val pendingBg: Color,
  val exemptText: Color,
  val exemptBg: Color,
)

val LocalMalabnaColors =
  staticCompositionLocalOf {
    MalabnaStatusColors(
      goingText = StatusGoingLight,
      goingBg = StatusGoingBgLight,
      notGoingText = StatusNotGoingLight,
      notGoingBg = StatusNotGoingBgLight,
      pendingText = StatusPendingLight,
      pendingBg = StatusPendingBgLight,
      exemptText = StatusExemptLight,
      exemptBg = StatusExemptBgLight,
    )
  }

private val LightStatusColors =
  MalabnaStatusColors(
    goingText = StatusGoingLight,
    goingBg = StatusGoingBgLight,
    notGoingText = StatusNotGoingLight,
    notGoingBg = StatusNotGoingBgLight,
    pendingText = StatusPendingLight,
    pendingBg = StatusPendingBgLight,
    exemptText = StatusExemptLight,
    exemptBg = StatusExemptBgLight,
  )

private val DarkStatusColors =
  MalabnaStatusColors(
    goingText = StatusGoingDark,
    goingBg = StatusGoingBgDark,
    notGoingText = StatusNotGoingDark,
    notGoingBg = StatusNotGoingBgDark,
    pendingText = StatusPendingDark,
    pendingBg = StatusPendingBgDark,
    exemptText = StatusExemptDark,
    exemptBg = StatusExemptBgDark,
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = EmeraldPrimaryDark,
    onPrimary = EmeraldOnPrimaryDark,
    primaryContainer = EmeraldContainerDark,
    onPrimaryContainer = EmeraldOnContainerDark,
    secondary = FloodlightSecondaryDark,
    onSecondary = FloodlightOnSecondaryDark,
    secondaryContainer = FloodlightContainerDark,
    onSecondaryContainer = FloodlightOnContainerDark,
    tertiary = TurfTertiaryDark,
    tertiaryContainer = TurfTertiaryContainerDark,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    error = ErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
  )

private val LightColorScheme =
  lightColorScheme(
    primary = EmeraldPrimaryLight,
    onPrimary = EmeraldOnPrimaryLight,
    primaryContainer = EmeraldContainerLight,
    onPrimaryContainer = EmeraldOnContainerLight,
    secondary = FloodlightSecondaryLight,
    onSecondary = FloodlightOnSecondaryLight,
    secondaryContainer = FloodlightContainerLight,
    onSecondaryContainer = FloodlightOnContainerLight,
    tertiary = TurfTertiaryLight,
    tertiaryContainer = TurfTertiaryContainerLight,
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    outline = LightOutline,
    error = ErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
  )

val MalabnaShapes =
  Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
  )

@Composable
fun MalabnaTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val statusColors = if (darkTheme) DarkStatusColors else LightStatusColors

  CompositionLocalProvider(LocalMalabnaColors provides statusColors) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = MalabnaTypography,
      shapes = MalabnaShapes,
      content = content,
    )
  }
}
