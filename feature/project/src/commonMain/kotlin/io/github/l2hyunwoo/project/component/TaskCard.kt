package io.github.l2hyunwoo.project.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.core.design.component.moon.Moon
import io.github.l2hyunwoo.data.tasks.model.Task
import io.github.l2hyunwoo.data.tasks.model.TaskPriority
import io.github.l2hyunwoo.data.tasks.model.fixture

@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val colors = KudosTheme.colors
    val cardShape = remember { RoundedCornerShape(20.dp) }
    val done = task.status.fraction >= 1f

    // Subtle press-scale; read inside graphicsLayer (draw phase) so press transitions skip
    // composition/layout. micro is a Float spec, so reduce-motion collapses it automatically.
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = KudosTheme.motion.micro,
        label = "taskCardPressScale",
    )
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(cardShape)
            .background(color = colors.surface.surface, shape = cardShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
    ) {
        // Left priority bar
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(task.priority.barColor())
        )

        Spacer(modifier = Modifier.width(12.dp))

        Moon(
            k = task.status.fraction,
            size = 22.dp,
            modifier = Modifier.align(Alignment.CenterVertically)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 12.dp)
        ) {
            Text(
                text = task.taskId,
                style = KudosTheme.typography.identifier,
                color = colors.ink.ink3
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = task.title,
                style = KudosTheme.typography.rowTitle,
                color = if (done) colors.ink.ink3 else colors.ink.ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${task.priority.text} · ${formatStatus(task.status.name)}",
                style = KudosTheme.typography.labelSmallM,
                color = colors.ink.ink2
            )
        }
    }
}

@Composable
private fun TaskPriority.barColor(): Color {
    val priority = KudosTheme.colors.priority
    return when (this) {
        TaskPriority.URGENT -> priority.urgent
        TaskPriority.HIGH -> priority.high
        TaskPriority.MEDIUM -> priority.medium
        TaskPriority.LOW -> priority.low
    }
}

private fun formatStatus(status: String): String {
    return status.lowercase()
        .split('_')
        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}

@Preview(showBackground = true)
@Composable
private fun TaskCardPreview() {
    KudosTheme {
        TaskCard(task = Task.fixture)
    }
}
