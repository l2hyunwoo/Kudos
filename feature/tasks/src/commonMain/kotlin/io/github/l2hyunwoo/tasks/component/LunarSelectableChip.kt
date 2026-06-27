package io.github.l2hyunwoo.tasks.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme

// Press scale for the micro tactile feedback; mirrors KudosButton's idiom so taps feel identical.
private const val PressedScale = 0.96f

/**
 * Lunar pill chip used for single-select rows (Priority, Due date). Selected fills periwinkle with
 * white content; unselected is an outlined surface chip. An optional [leadingDotColor] renders a small
 * colored dot before the label (priority chips); pass null for a plain chip (due-date chips).
 *
 * All params are stable (String/Boolean/Color/Modifier + a lambda), so with strong skipping the chip
 * skips recomposition when nothing changes — it is the small skippable unit itself.
 *
 * @param leadingDotColor null => no dot; set => an 8dp dot before the label.
 * @param enabled false => dimmed to alpha 0.4 and non-clickable.
 */
@Composable
fun LunarSelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingDotColor: Color? = null,
    enabled: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) PressedScale else 1f,
        animationSpec = KudosTheme.motion.micro,
        label = "chipPressScale",
    )

    val containerColor = if (selected) KudosTheme.colors.brand.primary600 else KudosTheme.colors.surface.surface
    val contentColor = if (selected) Color.White else KudosTheme.colors.ink.ink2
    // Border only on the unselected (outlined) state; the filled selected chip needs none.
    val borderColor = if (selected) Color.Transparent else KudosTheme.colors.surface.outline
    val shape = KudosTheme.shapes.pill

    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (enabled) 1f else 0.4f
            }
            .clip(shape)
            .background(containerColor)
            .then(if (borderColor != Color.Transparent) Modifier.border(1.dp, borderColor, shape) else Modifier)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled,
                role = Role.RadioButton,
                onClick = onClick,
            )
            .defaultMinSize(minHeight = 40.dp)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (leadingDotColor != null) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(leadingDotColor),
            )
        }
        Text(
            text = label,
            style = KudosTheme.typography.titleSmallM,
            color = contentColor,
        )
    }
}

@Preview
@Composable
private fun LunarSelectableChipLightPreview() {
    KudosTheme(darkTheme = false) {
        LunarSelectableChipPreviewRow()
    }
}

@Preview
@Composable
private fun LunarSelectableChipDarkPreview() {
    KudosTheme(darkTheme = true) {
        LunarSelectableChipPreviewRow()
    }
}

@Composable
private fun LunarSelectableChipPreviewRow() {
    Row(
        modifier = Modifier
            .background(KudosTheme.colors.surface.bg)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LunarSelectableChip(label = "Today", selected = true, onClick = {})
        LunarSelectableChip(label = "None", selected = false, onClick = {})
        LunarSelectableChip(
            label = "Urgent",
            selected = false,
            onClick = {},
            leadingDotColor = KudosTheme.colors.priority.urgent,
        )
    }
}
