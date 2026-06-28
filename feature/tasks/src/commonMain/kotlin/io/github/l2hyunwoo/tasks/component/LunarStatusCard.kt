package io.github.l2hyunwoo.tasks.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.core.design.component.moon.Moon
import io.github.l2hyunwoo.data.tasks.model.TaskStatus

// Press scale for the micro tactile feedback; mirrors KudosButton's idiom.
private const val PRESSED_SCALE = 0.96f

/**
 * Selectable status card: a moon-phase glyph over a centered label, used for the single-select Status
 * row. Selected fills the periwinkle tint with a periwinkle border; unselected is an outlined surface
 * card. Designed to sit in a Row with each card weighted so the four cards fill the row evenly.
 *
 * All params are stable (Float/String/Boolean/Modifier + a lambda), so the card skips recomposition
 * when nothing changes.
 *
 * @param fraction lit moon fraction (the status' [TaskStatus.fraction]), 0..1.
 */
@Composable
fun LunarStatusCard(
    fraction: Float,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) PRESSED_SCALE else 1f,
        animationSpec = KudosTheme.motion.micro,
        label = "statusCardPressScale",
    )

    val containerColor = if (selected) KudosTheme.colors.brand.primary100 else KudosTheme.colors.surface.surface
    val labelColor = if (selected) KudosTheme.colors.brand.primary600 else KudosTheme.colors.ink.ink2
    val borderColor = if (selected) KudosTheme.colors.brand.primary600 else KudosTheme.colors.surface.outline
    val borderWidth = if (selected) 1.5.dp else 1.dp
    val shape = KudosTheme.shapes.chipSmall

    Column(
        modifier =
            modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }.clip(shape)
                .background(containerColor)
                .border(borderWidth, borderColor, shape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.RadioButton,
                    onClick = onClick,
                ).padding(vertical = 12.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Moon(k = fraction, size = 28.dp)
        Text(
            text = label,
            style = KudosTheme.typography.labelLargeM,
            color = labelColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}

@Preview
@Composable
private fun LunarStatusCardLightPreview() {
    KudosTheme(darkTheme = false) {
        LunarStatusCardPreviewRow()
    }
}

@Preview
@Composable
private fun LunarStatusCardDarkPreview() {
    KudosTheme(darkTheme = true) {
        LunarStatusCardPreviewRow()
    }
}

@Composable
private fun LunarStatusCardPreviewRow() {
    Row(
        modifier =
            Modifier
                .background(KudosTheme.colors.surface.bg)
                .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LunarStatusCard(
            fraction = TaskStatus.BACKLOG.fraction,
            label = "Backlog",
            selected = false,
            onClick = {},
            modifier = Modifier.weight(1f),
        )
        LunarStatusCard(
            fraction = TaskStatus.TODO.fraction,
            label = "To Do",
            selected = true,
            onClick = {},
            modifier = Modifier.weight(1f),
        )
        LunarStatusCard(
            fraction = TaskStatus.IN_PROGRESS.fraction,
            label = "In Progress",
            selected = false,
            onClick = {},
            modifier = Modifier.weight(1f),
        )
        LunarStatusCard(
            fraction = TaskStatus.DONE.fraction,
            label = "Done",
            selected = false,
            onClick = {},
            modifier = Modifier.weight(1f),
        )
    }
}
