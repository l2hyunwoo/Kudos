package io.github.l2hyunwoo.core.design

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// 기본 색상 정의
private val Gray121212 = Color(0xFF121212)
private val GrayD9D9D9 = Color(0xFFD9D9D9)
private val Gray9F9F9F = Color(0xFF9F9F9F)
private val Gray222222 = Color(0xFF222222)
private val Gray393433 = Color(0xFF393433)
private val White = Color(0xFFFFFFFF)
private val OffWhite = Color(0xFFF3EFEE)
private val Black = Color(0xFF000000)
private val Peach = Color(0xFFD1A28B)
private val LightPeach = Color(0xFFE6D9CB)
private val LightGray = Color(0xFFF9F5F4)

// Light Theme ColorScheme
val LightColorScheme = lightColorScheme(
    primary = Gray121212,
    onPrimary = White,
    primaryContainer = GrayD9D9D9,
    onPrimaryContainer = Gray121212,

    secondary = Peach,
    onSecondary = White,
    secondaryContainer = LightPeach,
    onSecondaryContainer = Gray121212,

    tertiary = Gray9F9F9F,
    onTertiary = White,

    background = White,
    onBackground = Gray121212,

    surface = OffWhite,
    onSurface = Gray121212,
    surfaceVariant = LightGray,
    onSurfaceVariant = Gray222222,

    outline = Gray9F9F9F,
    outlineVariant = GrayD9D9D9,

    error = Color(0xFFBA1A1A),
    onError = White,
)

// Dark Theme ColorScheme
val DarkColorScheme = darkColorScheme(
    primary = GrayD9D9D9,
    onPrimary = Gray121212,
    primaryContainer = Gray393433,
    onPrimaryContainer = GrayD9D9D9,

    secondary = Peach,
    onSecondary = Gray121212,
    secondaryContainer = Gray222222,
    onSecondaryContainer = LightPeach,

    tertiary = Gray9F9F9F,
    onTertiary = Gray121212,

    background = Gray121212,
    onBackground = White,

    surface = Gray121212,
    onSurface = OffWhite,
    surfaceVariant = Gray222222,
    onSurfaceVariant = GrayD9D9D9,

    outline = Gray9F9F9F,
    outlineVariant = Gray393433,

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
)

val LocalColorScheme = staticCompositionLocalOf { LightColorScheme }

