package io.github.l2hyunwoo.core.design.component.moon

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme
import kotlin.math.abs
import kotlin.math.roundToInt

// Moon phase glyph. k in [0,1] is the lit fraction; the lit limb is always on the right (waxing).
// The unlit area is painted by a shadow Path (filled with shadowColor) over a fully-lit base disc.
//
// Terminator geometry (graphics-engineer verified): the naive rx=r*|1-2k| ellipse renders a mirror
// crescent for k>0.5. The fix is to flip the terminator arc's SWEEP SIGN at k=0.5; the outer limb
// stays fixed (start 270, sweep -180). With this, lit area == k and the lit limb stays on the right.
@Composable
fun Moon(
    k: Float,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
    litBrush: Brush = KudosTheme.colors.moon.litGradient,
    shadowColor: Color = KudosTheme.colors.surface.surface,
    ringColor: Color = KudosTheme.colors.moon.ring,
    glow: Boolean = k >= 1f,
) {
    val glowColor = KudosTheme.colors.moon.glow
    // Quantize k so the draw cache only rebuilds on visible phase steps (~0.5% increments).
    val quantK = (k.coerceIn(0f, 1f) * 200f).roundToInt()
    Box(
        modifier = modifier
            .size(size)
            .drawWithCache {
                val kClamped = quantK / 200f
                val side = this.size.minDimension
                val scale = side / VIEW_BOX
                val r = BASE_RADIUS * scale
                val cx = this.size.width / 2f
                val cy = this.size.height / 2f
                val stroke = STROKE_WIDTH * scale

                val circleBounds = Rect(cx - r, cy - r, cx + r, cy + r)
                val shadowPath = Path()
                if (kClamped < 1f) {
                    buildShadowPath(shadowPath, kClamped, cx, cy, r, circleBounds)
                }

                val glowBrush = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = 0.55f), Color.Transparent),
                    center = Offset(cx, cy),
                    radius = r * 1.6f,
                )

                onDrawBehind {
                    if (glow) {
                        drawCircle(brush = glowBrush, radius = r * 1.6f, center = Offset(cx, cy))
                    }
                    drawCircle(brush = litBrush, radius = r, center = Offset(cx, cy))
                    if (kClamped < 1f) {
                        drawPath(shadowPath, color = shadowColor)
                    }
                    drawCircle(
                        color = ringColor,
                        radius = r,
                        center = Offset(cx, cy),
                        style = Stroke(width = stroke),
                    )
                }
            },
    )
}

// Outer limb is fixed (top -> left -> bottom). The terminator's sweep sign flips at k=0.5.
private fun buildShadowPath(
    path: Path,
    k: Float,
    cx: Float,
    cy: Float,
    r: Float,
    circleBounds: Rect,
) {
    path.rewind()
    path.moveTo(cx, cy - r) // top
    path.arcTo(circleBounds, startAngleDegrees = 270f, sweepAngleDegrees = -180f, forceMoveTo = false)
    val rx = r * abs(1f - 2f * k)
    if (rx < 0.01f) {
        path.lineTo(cx, cy - r) // first quarter: straight terminator
    } else {
        val terminatorBounds = Rect(cx - rx, cy - r, cx + rx, cy + r)
        val sweep = if (k < 0.5f) -180f else 180f
        path.arcTo(terminatorBounds, startAngleDegrees = 90f, sweepAngleDegrees = sweep, forceMoveTo = false)
    }
    path.close()
}

private const val VIEW_BOX = 60f
private const val BASE_RADIUS = 22f
private const val STROKE_WIDTH = 2f

// Animated, tappable moon. The fill tweens between phases; completion triggers a one-shot glow that
// does NOT replay on recomposition nor fire for rows that are already done at first composition.
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MoonToggle(
    k: Float,
    onTap: () -> Unit,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 28.dp,
) {
    val animatedK by animateFloatAsState(
        targetValue = k.coerceIn(0f, 1f),
        animationSpec = KudosTheme.motion.moonFill,
    )
    val doneGlow = remember { Animatable(0f) }
    val isDone = k >= 1f
    // Seed wasDone with the initial done-state so an already-complete row does not flash on entry.
    val wasDone = remember { mutableStateOf(isDone) }
    LaunchedEffect(isDone) {
        if (isDone && !wasDone.value) {
            doneGlow.snapTo(1f)
            doneGlow.animateTo(0f, animationSpec = tween(LunarGlowDurationMs))
        }
        wasDone.value = isDone
    }

    val interaction = remember { MutableInteractionSource() }
    Moon(
        k = animatedK,
        modifier = modifier
            .size(size)
            .combinedClickable(
                interactionSource = interaction,
                indication = null,
                onClick = onTap,
                onLongClick = onLongPress,
            ),
        size = size,
        glow = animatedK >= 0.999f || doneGlow.value > 0f,
    )
}

private const val LunarGlowDurationMs = 520

// Progress glyph: k = done/total. Renders a Moon; the ring already conveys the boundary.
@Composable
fun MoonProgress(
    done: Int,
    total: Int,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
) {
    val k = if (total <= 0) 0f else (done.toFloat() / total.toFloat()).coerceIn(0f, 1f)
    Moon(k = k, modifier = modifier, size = size, glow = k >= 1f)
}

@Preview
@Composable
private fun MoonLightPreview() {
    KudosTheme(darkTheme = false) {
        MoonPreviewRow()
    }
}

@Preview
@Composable
private fun MoonDarkPreview() {
    KudosTheme(darkTheme = true) {
        MoonPreviewRow()
    }
}

@Composable
private fun MoonPreviewRow() {
    Row(
        modifier = Modifier.background(KudosTheme.colors.surface.bg).padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        listOf(0f, 0.5f, 0.8f, 1f).forEach { phase ->
            Moon(k = phase, size = 40.dp)
        }
    }
}
