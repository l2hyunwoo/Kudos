package io.github.l2hyunwoo.core.design

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// Lunar palette — periwinkle brand over cool neutrals.
private val Periwinkle600 = Color(0xFF6C63E6)
private val Periwinkle500 = Color(0xFF7A72EA)
private val Periwinkle400 = Color(0xFF948DF2)
private val Periwinkle100 = Color(0xFFECEBFB)
private val White = Color(0xFFFFFFFF)
private val Ink = Color(0xFF1B1A24)
private val Ink2 = Color(0xFF52505F)
private val Ink3 = Color(0xFF9A98A8)
private val Surface2Light = Color(0xFFFBFAFF)
private val OutlineVariantLight = Color(0xFFEAE9F2)
private val LunarError = Color(0xFFF2555A)

private val DarkBg = Color(0xFF100F16)
private val DarkSurface = Color(0xFF1A1922)
private val DarkSurfaceVariant = Color(0xFF222230)
private val DarkOnSurface = Color(0xFFF3F2FA)
private val DarkOnSurfaceVariant = Color(0xFFB4B2C4)
private val DarkOutline = Color(0xFF3A3950)
private val DarkOutlineVariant = Color(0xFF2B2A38)

val LightColorScheme =
    lightColorScheme(
        primary = Periwinkle600,
        onPrimary = White,
        primaryContainer = Periwinkle100,
        onPrimaryContainer = Ink,
        secondary = Periwinkle500,
        onSecondary = White,
        secondaryContainer = Periwinkle100,
        onSecondaryContainer = Ink,
        tertiary = Ink3,
        onTertiary = White,
        background = White,
        onBackground = Ink,
        surface = White,
        onSurface = Ink,
        surfaceVariant = Surface2Light,
        onSurfaceVariant = Ink2,
        outline = Ink3,
        outlineVariant = OutlineVariantLight,
        error = LunarError,
        onError = White,
    )

val DarkColorScheme =
    darkColorScheme(
        primary = Periwinkle400,
        onPrimary = Ink,
        primaryContainer = DarkSurfaceVariant,
        onPrimaryContainer = DarkOnSurface,
        secondary = Periwinkle500,
        onSecondary = Ink,
        secondaryContainer = DarkSurfaceVariant,
        onSecondaryContainer = DarkOnSurface,
        tertiary = Ink3,
        onTertiary = Ink,
        background = DarkBg,
        onBackground = DarkOnSurface,
        surface = DarkSurface,
        onSurface = DarkOnSurface,
        surfaceVariant = DarkSurfaceVariant,
        onSurfaceVariant = DarkOnSurfaceVariant,
        outline = DarkOutline,
        outlineVariant = DarkOutlineVariant,
        error = LunarError,
        onError = White,
    )

val LocalColorScheme = staticCompositionLocalOf { LightColorScheme }
