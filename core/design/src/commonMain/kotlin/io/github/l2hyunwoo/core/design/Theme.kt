package io.github.l2hyunwoo.core.design

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontFamily
import coil3.ColorImage
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler

val LocalDarkTheme = compositionLocalOf { true }

@OptIn(ExperimentalCoilApi::class)
@Composable
fun KudosTheme(
    fontFamily: FontFamily = FontFamily.Default,
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalDarkTheme provides darkTheme,
        LocalColorScheme provides if (darkTheme) {
            DarkColorScheme
        } else {
            LightColorScheme
        },
        LocalIndication provides ripple(),
        LocalTypography provides KudosTypography.with(fontFamily),
        LocalAsyncImagePreviewHandler provides AsyncImagePreviewHandler {
            ColorImage(Color.Red.toArgb())
        },
        content = content,
    )
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
}
