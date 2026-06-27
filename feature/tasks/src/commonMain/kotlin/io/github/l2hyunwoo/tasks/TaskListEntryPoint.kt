package io.github.l2hyunwoo.tasks

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.data.tasks.model.Task
import io.github.l2hyunwoo.kudos.core.common.compose.EventFlow
import io.github.l2hyunwoo.kudos.core.common.compose.rememberEventFlow
import io.github.l2hyunwoo.kudos.core.soil.SoilBoundary
import io.github.l2hyunwoo.kudos.core.soil.SoilFallbackDefaults
import soil.query.compose.rememberQuery

@OptIn(ExperimentalMaterial3Api::class)
@Composable
context(context: TasksContext)
fun TaskListEntryPoint(
    eventFlow: EventFlow<TaskListEvent>? = null,
    onAddTask: () -> Unit = {},
    onNavigateToCategories: () -> Unit = {},
    onNavigateToTaskDetail: (Task) -> Unit = {},
    searchQuery: String = "",
    // Top contentPadding so the list starts below the glass header owned by the parent (MainScreen)
    // and the rest scrolls under it. Standalone usage passes 0.
    topContentPadding: Dp = 0.dp,
) {
    val actualEventFlow = eventFlow ?: rememberEventFlow()

    SoilBoundary(
        state = rememberQuery(context.tasksQuery),
        fallback = SoilFallbackDefaults.appBar(
            title = "Tasks",
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onAddTask,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Task",
                    )
                }
            }
        ),
    ) { categories ->
        val uiState = taskListPresenter(
            eventFlow = actualEventFlow,
            categories = categories,
            searchQuery = searchQuery,
        )

        TaskListScreen(
            uiState = uiState,
            eventFlow = actualEventFlow,
            topContentPadding = topContentPadding,
            onTaskClick = onNavigateToTaskDetail,
        )
    }
}
