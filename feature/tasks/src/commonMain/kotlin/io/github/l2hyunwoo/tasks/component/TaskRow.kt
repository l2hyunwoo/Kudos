package io.github.l2hyunwoo.tasks.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.data.tasks.model.Task
import io.github.l2hyunwoo.data.tasks.model.fixture
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun TaskRow(
    task: Task,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = KudosTheme.colorScheme.surface,
                shape = RoundedCornerShape(12.dp)
            ).clip(shape = RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = task.status.text,
            style = KudosTheme.typography.bodyMediumR,
            color = KudosTheme.colorScheme.tertiary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = task.title,
            style = KudosTheme.typography.titleMediumR,
            color = KudosTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskRowPreview() {
    KudosTheme {
        TaskRow(
            task = Task.fixture,
        )
    }
}

