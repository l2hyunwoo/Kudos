package io.github.l2hyunwoo.core.design.token

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Periwinkle-tinted shadow specs. offsetY/blur are layout values; ambient is the shadow color.
// Drawn via a custom shadow modifier at consumer sites (Compose's elevation API has no colored shadow
// pre-API28; these tokens carry the design intent regardless of platform fallback).
@Immutable
data class KudosShadowSpec(
    val offsetY: Dp,
    val blur: Dp,
    val color: Color,
)

@Immutable
data class KudosElevation(
    val card: KudosShadowSpec =
        KudosShadowSpec(
            offsetY = 8.dp,
            blur = 30.dp,
            color = Color(0x24281C5A), // rgba(40,32,90,.14)
        ),
    val primaryButton: KudosShadowSpec =
        KudosShadowSpec(
            offsetY = 8.dp,
            blur = 22.dp,
            color = Color(0x806C63E6), // periwinkle glow @ 50%
        ),
)

val LocalElevation = staticCompositionLocalOf { KudosElevation() }
