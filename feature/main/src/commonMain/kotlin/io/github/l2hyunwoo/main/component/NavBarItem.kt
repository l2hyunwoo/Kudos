package io.github.l2hyunwoo.main.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.core.design.token.LUNAR_DURATION_MICRO
import io.github.l2hyunwoo.core.design.token.LUNAR_DURATION_STANDARD
import io.github.l2hyunwoo.core.design.token.LunarStandardEasing

// KudosMotion specs are FiniteAnimationSpec<Float>; color animation needs a Color spec, so rebuild one
// from the same standard duration + easing tokens. Hoisted to a file-level val so it is not re-allocated
// on every recomposition.
private val NavBarColorSpec = tween<Color>(LUNAR_DURATION_STANDARD, easing = LunarStandardEasing)

@Composable
internal fun NavBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val contentColor by animateColorAsState(
        targetValue = if (selected) KudosTheme.colors.brand.primary600 else KudosTheme.colors.ink.ink3,
        animationSpec = NavBarColorSpec,
    )
    val pillColor by animateColorAsState(
        targetValue =
            if (selected) {
                KudosTheme.colors.brand.primary100
            } else {
                Color.Transparent
            },
        animationSpec = NavBarColorSpec,
    )
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    // Subtle press feedback: dip the pill on press, settle on release. micro is a Float spec so
    // reduce-motion collapses it automatically.
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = KudosTheme.motion.micro,
        label = "navPillScale",
    )
    // Outer = the weighted slot; an inner horizontal margin keeps the highlighted pill off the
    // slot edge so its rounded corners stay intact. clip precedes clickable so the ripple is
    // bounded to the pill shape, not the raw rectangular slot.
    Box(
        modifier.padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }.clip(PillShape)
                .background(pillColor)
                .clickable(
                    interactionSource = interaction,
                    indication = ripple(bounded = true),
                    onClick = onClick,
                ).padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp),
            )
            // The label expands+fades in on selection instead of snapping, so the pill grows into
            // its selected width. Typed specs rebuilt from the micro duration + standard easing.
            AnimatedVisibility(
                visible = selected,
                enter =
                    expandHorizontally(
                        animationSpec = tween(LUNAR_DURATION_STANDARD, easing = LunarStandardEasing),
                    ) + fadeIn(animationSpec = tween(LUNAR_DURATION_STANDARD, easing = LunarStandardEasing)),
                exit =
                    shrinkHorizontally(
                        animationSpec = tween(LUNAR_DURATION_MICRO, easing = LunarStandardEasing),
                    ) + fadeOut(animationSpec = tween(LUNAR_DURATION_MICRO, easing = LunarStandardEasing)),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(8.dp))
                    Text(text = label, style = KudosTheme.typography.labelLargeM, color = contentColor)
                }
            }
        }
    }
}
