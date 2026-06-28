package io.github.l2hyunwoo.core.design.token

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.lerp

// Semantic color holder, mirroring KudosTypography. Provided via staticCompositionLocalOf and
// exposed on KudosTheme.colors. Domain-free: features map TaskStatus/priority onto these tokens.

@Immutable
data class KudosBrand(
    val primary600: Color,
    val primary500: Color,
    val primary400: Color,
    val primary200: Color,
    val primary100: Color,
    val primary050: Color,
)

@Immutable
data class KudosInk(
    val ink: Color,
    val ink2: Color,
    val ink3: Color,
)

@Immutable
data class KudosSurface(
    val surface: Color,
    val surface2: Color,
    val bg: Color,
    val outline: Color,
    val outlineStrong: Color,
)

@Immutable
data class KudosPastels(
    val lilac: Color,
    val mint: Color,
    val sky: Color,
    val peach: Color,
    val butter: Color,
    val rose: Color,
)

@Immutable
data class KudosPriority(
    val urgent: Color,
    val high: Color,
    val medium: Color,
    val low: Color,
)

@Immutable
data class KudosGlass(
    val fill: Color,
    val border: Color,
    val shadowTint: Color,
    // sheetFill is the bottom-sheet variant of [fill]. A ModalBottomSheet renders in its own
    // window above the app content and cannot read the screen's `sky` backdrop recorder, so it
    // can't do a real backdrop blur the way `glassSurface(sky)` does. sheetFill therefore carries
    // a HIGHER alpha than [fill]: an honest translucent-frost slab over the scrim (not a fake
    // backdrop blur), with the same periwinkle tint so it reads as the same glass material family.
    val sheetFill: Color,
    // Soft dark scrim behind floating chrome (e.g. bottom-sheet scrim). Periwinkle-tinted dark
    // rather than flat black so the dim matches the moonlight palette.
    val scrim: Color,
)

@Immutable
data class KudosMoon(
    val litGradient: Brush,
    val ring: Color,
    val glow: Color,
)

@Immutable
data class KudosColors(
    val brand: KudosBrand,
    val ink: KudosInk,
    val surface: KudosSurface,
    val pastels: KudosPastels,
    val priority: KudosPriority,
    val glass: KudosGlass,
    val moon: KudosMoon,
) {
    // chip background = dot @ 12% alpha composited over surface; foreground = darkened dot for AA.
    fun pastelChip(dot: Color): Pair<Color, Color> {
        val bg = dot.copy(alpha = 0.12f).compositeOver(surface.surface)
        val fg = lerp(dot, ink.ink, 0.45f)
        return bg to fg
    }
}

private val LunarPastels =
    KudosPastels(
        lilac = Color(0xFFC9B8F0),
        mint = Color(0xFFA6E3C9),
        sky = Color(0xFFA8CDF2),
        peach = Color(0xFFF4BFA6),
        butter = Color(0xFFF2D89C),
        rose = Color(0xFFF2B5C8),
    )

private val LunarPriority =
    KudosPriority(
        urgent = Color(0xFFF2555A),
        high = Color(0xFFF2994A),
        medium = Color(0xFFEAC44E),
        low = Color(0xFF54C08A),
    )

fun lightKudosColors(): KudosColors {
    val surface = Color(0xFFFFFFFF)
    return KudosColors(
        brand =
            KudosBrand(
                primary600 = Color(0xFF6C63E6),
                primary500 = Color(0xFF7A72EA),
                primary400 = Color(0xFF948DF2),
                primary200 = Color(0xFFC9C4F7),
                primary100 = Color(0xFFECEBFB),
                primary050 = Color(0xFFF5F4FD),
            ),
        ink =
            KudosInk(
                ink = Color(0xFF1B1A24),
                ink2 = Color(0xFF52505F),
                ink3 = Color(0xFF9A98A8),
            ),
        surface =
            KudosSurface(
                surface = surface,
                surface2 = Color(0xFFFBFAFF),
                bg = Color(0xFFFFFFFF),
                outline = Color(0xFFEAE9F2),
                outlineStrong = Color(0xFFD7D5E6),
            ),
        pastels = LunarPastels,
        priority = LunarPriority,
        glass =
            KudosGlass(
                // Faint periwinkle (primary050) at a lower alpha than plain white: over an empty/white
                // backdrop the old White@0.55 read as a solid white slab. The cool tint + reduced alpha
                // makes it frosted while keeping ink text legible.
                fill = Color(0xFFF5F4FD).copy(alpha = 0.44f),
                border = Color.White.copy(alpha = 0.6f),
                shadowTint = Color(0x24281C5A), // rgba(40,32,90,.14)
                // Faint periwinkle wash (primary050) at high alpha: legible over the scrim while still
                // reading cooler/frostier than a plain white slab.
                sheetFill = Color(0xFFF5F4FD).copy(alpha = 0.94f),
                // rgba(40,32,90,.32) — soft periwinkle-tinted dim.
                scrim = Color(0x52281C5A),
            ),
        moon =
            KudosMoon(
                litGradient =
                    Brush.linearGradient(
                        // Lit from the right: brighter periwinkle on the lit limb.
                        listOf(Color(0xFF6C63E6), Color(0xFF948DF2)),
                    ),
                ring = Color(0xFFD7D5E6),
                glow = Color(0xFF948DF2),
            ),
    )
}

fun darkKudosColors(): KudosColors {
    val surface = Color(0xFF1A1922)
    return KudosColors(
        brand =
            KudosBrand(
                primary600 = Color(0xFF6C63E6),
                primary500 = Color(0xFF7A72EA),
                primary400 = Color(0xFF948DF2),
                primary200 = Color(0xFFC9C4F7),
                // 100/050 are the "selected" tint backgrounds (nav pill, chips, theme toggle). On dark
                // they must be dark periwinkle tints that sit between surface2 and outlineStrong — the
                // light near-white values (#ECEBFB/#F5F4FD) read as glaring white slabs over the dark BG.
                primary100 = Color(0xFF322F4D),
                primary050 = Color(0xFF272539),
            ),
        ink =
            KudosInk(
                ink = Color(0xFFF3F2FA),
                ink2 = Color(0xFFB4B2C4),
                ink3 = Color(0xFF9A98A8),
            ),
        surface =
            KudosSurface(
                surface = surface,
                surface2 = Color(0xFF222230),
                bg = Color(0xFF100F16),
                outline = Color(0xFF2B2A38),
                outlineStrong = Color(0xFF3A3950),
            ),
        pastels = LunarPastels,
        priority = LunarPriority,
        glass =
            KudosGlass(
                fill = Color(0xFF1A1922).copy(alpha = 0.55f),
                border = Color.White.copy(alpha = 0.12f),
                shadowTint = Color(0x33000000),
                // Dark surface2 at high alpha — a frosted night-glass slab that stays above the bg.
                sheetFill = Color(0xFF222230).copy(alpha = 0.96f),
                // Deeper dim on dark per the spec's raised-sheet shadow family (rgba(8,6,20,.7)).
                scrim = Color(0xB3080614),
            ),
        moon =
            KudosMoon(
                litGradient =
                    Brush.linearGradient(
                        listOf(Color(0xFF948DF2), Color(0xFFC9C4F7)),
                    ),
                ring = Color(0xFF3A3950),
                glow = Color(0xFF948DF2),
            ),
    )
}

val LocalKudosColors = staticCompositionLocalOf { lightKudosColors() }
