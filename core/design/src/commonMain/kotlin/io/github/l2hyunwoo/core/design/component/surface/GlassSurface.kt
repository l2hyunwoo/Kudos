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
 * @param elevation shadow elevation; rendered unconditionally.
 * @param blurRadius backdrop blur radius in px (sigma handled inside Cloudy).
 */
@Composable
public fun Modifier.glassSurface(
    sky: Sky,
    shape: Shape,
    tint: Color = KudosTheme.colors.glass.fill,
    border: BorderStroke = BorderStroke(1.dp, KudosTheme.colors.glass.border),
    elevation: Dp = 8.dp,
    blurRadius: Int = 18,
): Modifier = this
    .shadow(elevation, shape)
    .clip(shape)
    // cpuBlurEnabled=false: native CPU-blur .so is not vendored, so API<31 uses the static
    // translucent scrim fallback. saturate is omitted (tint-only material for now).
    .cloudy(sky = sky, radius = blurRadius, tint = tint, cpuBlurEnabled = false)
    .border(border, shape)
