package io.github.l2hyunwoo.tasks

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import io.github.l2hyunwoo.kudos.core.soil.SoilBoundary
import io.github.l2hyunwoo.kudos.core.soil.SoilFallbackDefaults
import kotlinx.collections.immutable.toImmutableList
import soil.query.compose.rememberQuery

@OptIn(ExperimentalMaterial3Api::class)
@Composable
context(context: TasksContext)
fun TaskListEntryPoint() {
    SoilBoundary(
        state = rememberQuery(context.tasksQuery),
        fallback = SoilFallbackDefaults.appBar(
            title = "Tasks",
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { /* TODO: Add task */ },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Task",
                    )
                }
            }
        ),
    ) { categories ->
        TaskListScreen(
            categories = categories.toImmutableList()
        )
    }
}
