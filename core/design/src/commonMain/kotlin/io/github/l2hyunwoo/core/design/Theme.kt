package io.github.l2hyunwoo.core.design

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import io.github.l2hyunwoo.core.design.token.KudosColors
import io.github.l2hyunwoo.core.design.token.KudosMotion
import io.github.l2hyunwoo.core.design.token.LocalKudosColors
import io.github.l2hyunwoo.core.design.token.LocalMotion
import io.github.l2hyunwoo.core.design.token.LocalReduceMotion
import io.github.l2hyunwoo.core.design.token.darkKudosColors
import io.github.l2hyunwoo.core.design.token.kudosMotion
import io.github.l2hyunwoo.core.design.token.lightKudosColors

val LocalDarkTheme = compositionLocalOf { true }

@OptIn(ExperimentalCoilApi::class)
@Composable
fun KudosTheme(
    // Heading font (display/headline/title) defaults to Space Grotesk; body/label to Plus Jakarta Sans.
    displayFontFamily: FontFamily = spaceGroteskFamily(),
    bodyFontFamily: FontFamily = plusJakartaSansFamily(),
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val kudosColors = remember(darkTheme) { if (darkTheme) darkKudosColors() else lightKudosColors() }
    val reduceMotion = LocalReduceMotion.current
    val motion = remember(reduceMotion) { kudosMotion(reduceMotion) }
    val typography = remember(displayFontFamily, bodyFontFamily) {
        materialTypography(displayFontFamily, bodyFontFamily)
    }
    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalColorScheme provides colorScheme,
        LocalKudosColors provides kudosColors,
        LocalMotion provides motion,
        LocalIndication provides ripple(),
        LocalTypography provides KudosTypography.with(displayFontFamily, bodyFontFamily),
        LocalAsyncImagePreviewHandler provides AsyncImagePreviewHandler {
            ColorImage(Color.Red.toArgb())
        },
    ) {
        // MaterialTheme drives MaterialTheme.typography/colorScheme consumers and the global
        // LocalTextStyle, so unstyled Text and MaterialTheme.typography.* pick up the app fonts.
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content,
        )
    }
}

object KudosTheme {

    val colorScheme: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalColorScheme.current

    val typography: KudosTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current

    val colors: KudosColors
        @Composable
        @ReadOnlyComposable
        get() = LocalKudosColors.current

    val motion: KudosMotion
        @Composable
        @ReadOnlyComposable
        get() = LocalMotion.current
}
