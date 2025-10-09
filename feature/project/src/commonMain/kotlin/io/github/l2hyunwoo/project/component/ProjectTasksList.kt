package io.github.l2hyunwoo.project.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.data.tasks.model.Task
import kotlinx.collections.immutable.ImmutableList

@Composable
fun ProjectTasksList(
    tasks: ImmutableList<Task>,
    onTaskClick: (Task) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Section Header
        Text(
            text = "📋 연관된 이슈 (${tasks.size})",
            style = KudosTheme.typography.titleMediumB,
            color = KudosTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (tasks.isEmpty()) {
            Text(
                text = "연관된 이슈가 없습니다",
                style = KudosTheme.typography.bodySmallR,
                color = KudosTheme.colorScheme.tertiary,
                modifier = Modifier.padding(start = 16.dp)
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tasks.forEach { task ->
                    TaskCard(
                        task = task,
                        onClick = { onTaskClick(task) }
                    )
                }
            }
        }
    }
}
