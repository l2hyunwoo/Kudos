package io.github.l2hyunwoo.core.design.component.button

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme

// Lunar button set (spec section 06 Components): Primary (filled periwinkle + glow), Secondary
// (tint), Ghost (outline). All three share one skippable core — KudosButtonCore — so press feedback,
// shape, ripple, and the disabled treatment stay identical across variants. Each public button is a
// thin wrapper that picks colors/shadow from KudosTheme tokens, keeping the public API close to the
// Material Button signature (onClick, modifier, enabled, + a RowScope content slot) so they drop in
// where raw Material buttons were used.

/** Press scale for the micro tactile feedback; mirrors the app's other tap interactions. */
private const val PressedScale = 0.96f

/**
 * Filled periwinkle primary action with the periwinkle glow shadow.
 *
 * Color: [KudosTheme.colors] `brand.primary600` fill, white content.
 * Shadow: [KudosTheme.elevation] `primaryButton` glow (`0 8px 22px rgba(108,99,230,.5)` family).
 * Shape: pill ([KudosTheme.shapes] `pill`).
 */
@Composable
fun PrimaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = KudosTheme.shapes.pill,
    content: @Composable RowScope.() -> Unit,
) {
    val glow = KudosTheme.elevation.primaryButton
    KudosButtonCore(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        containerColor = KudosTheme.colors.brand.primary600,
        contentColor = Color.White,
        // Colored glow shadow drawn unconditionally; on a light backdrop the default neutral shadow
        // reads grey, so the periwinkle token is passed as both ambient and spot color.
        shadowElevation = glow.offsetY,
        shadowColor = glow.color,
        content = content,
    )
}

/**
 * Quieter filled variant on a periwinkle tint.
 *
 * Color: [KudosTheme.colors] `brand.primary100` tint fill, `primary600` content. No glow.
 */
@Composable
fun SecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = KudosTheme.shapes.pill,
    content: @Composable RowScope.() -> Unit,
) {
    KudosButtonCore(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        containerColor = KudosTheme.colors.brand.primary100,
        contentColor = KudosTheme.colors.brand.primary600,
        shadowElevation = 0.dp,
        shadowColor = Color.Transparent,
        content = content,
    )
}

/**
 * Transparent outlined action (ghost).
 *
 * Color: transparent fill, `outlineStrong` border, `primary600` content. Use for cancel/secondary
 * destinations that should recede next to a [PrimaryButton].
 */
@Composable
fun GhostButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    shape: Shape = KudosTheme.shapes.pill,
    content: @Composable RowScope.() -> Unit,
) {
    KudosButtonCore(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = shape,
        containerColor = Color.Transparent,
        contentColor = KudosTheme.colors.brand.primary600,
        borderColor = KudosTheme.colors.surface.outlineStrong,
        shadowElevation = 0.dp,
        shadowColor = Color.Transparent,
        content = content,
    )
}

/**
 * Shared skippable core. All params are stable (Color/Dp/Shape/Boolean + a content lambda), so the
 * Compose compiler can skip this when nothing changes between recompositions.
 */
@Composable
private fun KudosButtonCore(
    onClick: () -> Unit,
    enabled: Boolean,
    shape: Shape,
    containerColor: Color,
    contentColor: Color,
    shadowElevation: Dp,
    shadowColor: Color,
    modifier: Modifier = Modifier,
    borderColor: Color = Color.Transparent,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
    content: @Composable RowScope.() -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) PressedScale else 1f,
        animationSpec = KudosTheme.motion.micro,
        label = "buttonPressScale",
    )
    // Disabled treatment: keep the same box, just dim the whole button so the layout never shifts.
    val disabledAlpha = if (enabled) 1f else 0.4f
    val resolvedBorder = if (borderColor == Color.Transparent) null else borderColor

    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = disabledAlpha
            }
            // Glow/shadow under the fill so the colored shadow isn't clipped by the rounded fill.
            .then(
                if (shadowElevation > 0.dp) {
                    Modifier.shadow(
                        elevation = shadowElevation,
                        shape = shape,
                        ambientColor = shadowColor,
                        spotColor = shadowColor,
                    )
                } else {
                    Modifier
                }
            )
            .clip(shape)
            .background(containerColor)
            .then(if (resolvedBorder != null) Modifier.border(1.dp, resolvedBorder, shape) else Modifier)
            .clickableButton(
                interactionSource = interactionSource,
                enabled = enabled,
                onClick = onClick,
            )
            .defaultMinSize(minHeight = 48.dp)
            .padding(contentPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            ProvideTextStyle(KudosTheme.typography.labelLargeM) {
                content()
            }
        }
    }
}

// Bounded ripple via the theme's LocalIndication (set in KudosTheme), gated on `enabled`.
@Composable
private fun Modifier.clickableButton(
    interactionSource: MutableInteractionSource,
    enabled: Boolean,
    onClick: () -> Unit,
): Modifier = this.clickable(
    interactionSource = interactionSource,
    indication = LocalIndication.current,
    enabled = enabled,
    role = Role.Button,
    onClick = onClick,
)

@Preview
@Composable
private fun KudosButtonsLightPreview() {
    KudosTheme(darkTheme = false) {
        ButtonPreviewColumn()
    }
}

@Preview
@Composable
private fun KudosButtonsDarkPreview() {
    KudosTheme(darkTheme = true) {
        ButtonPreviewColumn()
    }
}

@Composable
private fun ButtonPreviewColumn() {
    Column(
        modifier = Modifier
            .background(KudosTheme.colors.surface.bg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PrimaryButton(onClick = {}) { Text("Primary") }
        SecondaryButton(onClick = {}) { Text("Secondary") }
        GhostButton(onClick = {}) { Text("Ghost") }
        PrimaryButton(onClick = {}, enabled = false) { Text("Disabled") }
    }
}
