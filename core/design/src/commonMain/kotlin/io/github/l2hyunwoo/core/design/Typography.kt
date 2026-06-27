package io.github.l2hyunwoo.core.design

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Material3 Typography wired to the two app fonts, so any MaterialTheme.typography.* consumer and
// every unstyled Text (LocalTextStyle defaults to typography.bodyLarge) renders the app fonts
// instead of the default Roboto. Display/headline/title slots = heading font, body/label = body font.
internal fun materialTypography(
    displayFontFamily: FontFamily,
    bodyFontFamily: FontFamily,
): Typography {
    val base = Typography()
    return base.copy(
        displayLarge = base.displayLarge.copy(fontFamily = displayFontFamily),
        displayMedium = base.displayMedium.copy(fontFamily = displayFontFamily),
        displaySmall = base.displaySmall.copy(fontFamily = displayFontFamily),
        headlineLarge = base.headlineLarge.copy(fontFamily = displayFontFamily),
        headlineMedium = base.headlineMedium.copy(fontFamily = displayFontFamily),
        headlineSmall = base.headlineSmall.copy(fontFamily = displayFontFamily),
        titleLarge = base.titleLarge.copy(fontFamily = displayFontFamily),
        titleMedium = base.titleMedium.copy(fontFamily = displayFontFamily),
        titleSmall = base.titleSmall.copy(fontFamily = displayFontFamily),
        bodyLarge = base.bodyLarge.copy(fontFamily = bodyFontFamily),
        bodyMedium = base.bodyMedium.copy(fontFamily = bodyFontFamily),
        bodySmall = base.bodySmall.copy(fontFamily = bodyFontFamily),
        labelLarge = base.labelLarge.copy(fontFamily = bodyFontFamily),
        labelMedium = base.labelMedium.copy(fontFamily = bodyFontFamily),
        labelSmall = base.labelSmall.copy(fontFamily = bodyFontFamily),
    )
}

@Immutable
data class KudosTypography(
    // Title-family base (display/headline/title styles). Carries the heading font.
    val displayDefault: TextStyle,
    // Body-family base (body/label styles). Carries the body font.
    val bodyDefault: TextStyle,

    val displayLargeR: TextStyle = displayDefault.copy(
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp,
    ),
    val displayMediumR: TextStyle = displayDefault.copy(
        fontSize = 45.sp,
        lineHeight = 52.sp,
    ),

    val displayMediumEB: TextStyle = displayDefault.copy(
        fontSize = 45.sp,
        lineHeight = 52.sp,
        fontWeight = FontWeight.ExtraBold,
    ),
    val displaySmallR: TextStyle = displayDefault.copy(
        fontSize = 36.sp,
        lineHeight = 44.sp,
    ),

    val headlineLargeEB: TextStyle = displayDefault.copy(
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.ExtraBold,
    ),
    val headlineLargeSB: TextStyle = displayDefault.copy(
        fontSize = 32.sp,
        lineHeight = 40.sp,
        fontWeight = FontWeight.SemiBold,
    ),
    val headlineLargeR: TextStyle = displayDefault.copy(
        fontSize = 32.sp,
        lineHeight = 40.sp,
    ),
    val headlineMediumB: TextStyle = displayDefault.copy(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Bold,
    ),
    val headlineMediumM: TextStyle = displayDefault.copy(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Medium,
    ),
    val headlineMediumR: TextStyle = displayDefault.copy(
        fontSize = 28.sp,
        lineHeight = 36.sp,
    ),
    val headlineSmallBL: TextStyle = displayDefault.copy(
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.Black,
        letterSpacing = (-0.2).sp,
    ),
    val headlineSmallM: TextStyle = displayDefault.copy(
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.Medium,
    ),
    val headlineSmallR: TextStyle = displayDefault.copy(
        fontSize = 24.sp,
        lineHeight = 32.sp,
    ),

    val titleLargeBL: TextStyle = displayDefault.copy(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Black,
    ),
    val titleLargeB: TextStyle = displayDefault.copy(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Bold,
    ),
    val titleLargeM: TextStyle = displayDefault.copy(
        fontSize = 22.sp,
        lineHeight = 28.sp,
        fontWeight = FontWeight.Medium,
    ),
    val titleLargeR: TextStyle = displayDefault.copy(
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    val titleMediumBL: TextStyle = displayDefault.copy(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Black,
    ),
    val titleMediumB: TextStyle = displayDefault.copy(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.Bold,
    ),
    val titleMediumR: TextStyle = displayDefault.copy(
        fontSize = 16.sp,
        lineHeight = 24.sp,
    ),
    val titleSmallB: TextStyle = displayDefault.copy(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.25.sp,
    ),
    val titleSmallM: TextStyle = displayDefault.copy(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.25.sp,
    ),
    val titleSmallM140: TextStyle = displayDefault.copy(
        fontSize = 14.sp,
        lineHeight = (19.6).sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = (-0.2).sp,
    ),
    val titleSmallR: TextStyle = displayDefault.copy(
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    val titleSmallR140: TextStyle = displayDefault.copy(
        fontSize = 14.sp,
        lineHeight = (19.6).sp,
        letterSpacing = (-0.2).sp,
    ),

    val labelLargeM: TextStyle = bodyDefault.copy(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
    ),
    val labelMediumR: TextStyle = bodyDefault.copy(
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),
    val labelSmallM: TextStyle = bodyDefault.copy(
        fontSize = 11.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = (-0.2).sp,
    ),

    val bodyLargeR: TextStyle = bodyDefault.copy(
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp,
    ),
    val bodyMediumR: TextStyle = bodyDefault.copy(
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp,
    ),
    val bodySmallR: TextStyle = bodyDefault.copy(
        fontSize = 12.sp,
        lineHeight = 16.sp,
    ),

    // Lunar additions.
    // Small uppercase section label (Space Grotesk), wide tracking.
    val eyebrow: TextStyle = displayDefault.copy(
        fontSize = 12.5.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.2.sp,
    ),
    // Monospaced-figure identifier (KUDOS-142). Tabular figures keep digit columns aligned.
    val identifier: TextStyle = displayDefault.copy(
        fontSize = 12.sp,
        lineHeight = 16.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.5.sp,
        fontFeatureSettings = "tnum",
    ),
    // Extra-bold body headline (Plus Jakarta).
    val bodyLargeXB: TextStyle = bodyDefault.copy(
        fontSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.ExtraBold,
    ),
    // Task/list row title (Plus Jakarta).
    val rowTitle: TextStyle = bodyDefault.copy(
        fontSize = 15.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.SemiBold,
    ),
) {
    companion object Companion {
        fun with(
            displayFontFamily: FontFamily = FontFamily.Default,
            bodyFontFamily: FontFamily = FontFamily.Default,
            fontWeight: FontWeight = FontWeight.Normal,
        ) = KudosTypography(
            displayDefault = TextStyle(
                fontFamily = displayFontFamily,
                fontWeight = fontWeight,
            ),
            bodyDefault = TextStyle(
                fontFamily = bodyFontFamily,
                fontWeight = fontWeight,
            ),
        )
    }
}

internal val LocalTypography = staticCompositionLocalOf<KudosTypography> {
    error("Should provide KudosTypography")
}
