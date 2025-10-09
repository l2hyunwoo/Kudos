package io.github.l2hyunwoo.project.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.data.tasks.model.Task

@Composable
fun TaskCard(
    task: Task,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = KudosTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Row {
            // Priority Color Bar (왼쪽)
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(Color(task.priority.color))
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                // Task ID + Status Emoji
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = task.status.text, // 🌓🌔🌕🌑
                        style = KudosTheme.typography.titleSmallR
                    )
                    Text(
                        text = task.taskId, // "WORK-123"
                        style = KudosTheme.typography.labelMediumR,
                        color = KudosTheme.colorScheme.secondary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Task Title
                Text(
                    text = task.title,
                    style = KudosTheme.typography.bodyMediumR,
                    color = KudosTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Priority · Status Labels
                Text(
                    text = "${task.priority.text} · ${formatStatus(task.status.name)}",
                    style = KudosTheme.typography.labelSmallM,
                    color = KudosTheme.colorScheme.tertiary
                )
            }
        }
    }
}

private fun formatStatus(status: String): String {
    return status.lowercase()
        .split('_')
        .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
}
