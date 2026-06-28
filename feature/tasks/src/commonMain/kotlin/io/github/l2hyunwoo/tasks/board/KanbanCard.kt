package io.github.l2hyunwoo.tasks.board

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.data.tasks.model.Task
import io.github.l2hyunwoo.data.tasks.model.TaskPriority
import io.github.l2hyunwoo.data.tasks.model.fixture
import io.github.l2hyunwoo.tasks.formatDueLabel
import io.github.l2hyunwoo.tasks.isOverdue
import io.github.l2hyunwoo.tasks.todayIso

// Compact board card: priority dot + title + (optional) due chip. No swipe (the board uses drag for
// status moves), no moon toggle (status is encoded by the column it sits in).
@Composable
fun KanbanCard(
    task: Task,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .clip(KudosTheme.shapes.row)
                .background(KudosTheme.colors.surface.surface)
                .clickable(onClick = onClick)
                .padding(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PriorityDot(task.priority)
            Spacer(Modifier.width(8.dp))
            Text(
                text = task.title,
                style = KudosTheme.typography.rowTitle,
                color = KudosTheme.colors.ink.ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = task.taskId.uppercase(),
                style = KudosTheme.typography.identifier,
                color = KudosTheme.colors.ink.ink3,
                maxLines = 1,
            )
            task.dueDate?.let { due ->
                val today = todayIso()
                val overdue = isOverdue(due, today)
                Text(
                    text = formatDueLabel(due, today),
                    style = KudosTheme.typography.labelLargeM,
                    color = if (overdue) KudosTheme.colors.priority.urgent else KudosTheme.colors.ink.ink2,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun PriorityDot(priority: TaskPriority) {
    Box(
        modifier =
            Modifier
                .size(8.dp)
                .clip(KudosTheme.shapes.pill)
                .background(priority.dotColor()),
    )
}

@Composable
private fun TaskPriority.dotColor(): Color {
    val p = KudosTheme.colors.priority
    return when (this) {
        TaskPriority.URGENT -> p.urgent
        TaskPriority.HIGH -> p.high
        TaskPriority.MEDIUM -> p.medium
        TaskPriority.LOW -> p.low
    }
}

@Preview(showBackground = true)
@Composable
private fun KanbanCardPreview() {
    KudosTheme {
        KanbanCard(task = Task.fixture)
    }
}
