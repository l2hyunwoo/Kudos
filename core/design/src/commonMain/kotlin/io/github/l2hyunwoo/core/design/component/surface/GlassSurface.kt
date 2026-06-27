package io.github.l2hyunwoo.core.design.component.surface

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.skydoves.cloudy.Sky
import com.skydoves.cloudy.cloudy
import io.github.l2hyunwoo.core.design.KudosTheme

/**
 * Frosted-glass chrome surface backed by the vendored Cloudy backdrop blur.
 *
 * shadow + clip + border + shape are applied UNCONDITIONALLY so every platform fallback path
 * (Android 31+ RenderEffect, iOS Skia BlurEffect, API24-30 translucent scrim) shares an identical
 * box and produces zero layout shift. Only the blurred fill differs per platform, driven by [cloudy].
 *
 * The caller hoists [rememberSky] at the screen root, puts `Modifier.sky(sky)` on the scrollable
 * content container, and each chrome surface calls `glassSurface(sky = ...)`.
 *
 * @param sky backdrop recorder shared with the content's `Modifier.sky(sky)`.
 * @param shape clip + shadow + border outline; reuse the same [Shape] for all three to stay aligned.
 * @param tint color blended over the blurred backdrop (glass fill).
 * @param border hairline stroke; defaults to the glass border token.
 * @param shadowColor ambient + spot shadow color; defaults to the periwinkle glass shadow token so
 *   the float reads as a soft on-brand tint instead of Compose's default neutral/black shadow.
 * @param elevation shadow elevation; rendered unconditionally.
 * @param blurRadius backdrop blur radius in px (sigma handled inside Cloudy).
 */
@Composable
public fun Modifier.glassSurface(
    sky: Sky,
    shape: Shape,
    tint: Color = KudosTheme.colors.glass.fill,
    border: BorderStroke = BorderStroke(1.dp, KudosTheme.colors.glass.border),
    shadowColor: Color = KudosTheme.colors.glass.shadowTint,
    elevation: Dp = 6.dp,
    blurRadius: Int = 18,
): Modifier = this
    // Pass the periwinkle shadow token as both ambient and spot color; the default overload uses
    // DefaultShadowColor (neutral), which on a light backdrop renders as the muddy gray box.
    .shadow(elevation, shape, ambientColor = shadowColor, spotColor = shadowColor)
    .clip(shape)
    // cpuBlurEnabled=false: native CPU-blur .so is not vendored, so API<31 uses the static
    // translucent scrim fallback. saturate is omitted (tint-only material for now).
    // Pass `shape` so the blurred fill is clipped to the rounded surface instead of a hard
    // rectangle, eliminating the inner "white box" seam at the rounded corners.
    .cloudy(sky = sky, radius = blurRadius, tint = tint, cpuBlurEnabled = false, shape = shape)
    .border(border, shape)
