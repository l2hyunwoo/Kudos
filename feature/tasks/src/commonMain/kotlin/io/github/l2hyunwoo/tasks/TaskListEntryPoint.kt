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
    // When embedded in MainScreen, the glass header + nav bar + FAB are owned by the parent and
    // hoisted OUTSIDE the backdrop recorder. The loading/error fallback must then carry NO chrome of
    // its own: an AppBar + FAB here would render inside the recorder and bleed through the glass
    // (title under the header, FAB under the nav bar). Standalone usage keeps the app-bar fallback.
    embedded: Boolean = false,
) {
    val actualEventFlow = eventFlow ?: rememberEventFlow()

    val fallback = if (embedded) {
        SoilFallbackDefaults.default()
    } else {
        SoilFallbackDefaults.appBar(
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
        )
    }

    SoilBoundary(
        state = rememberQuery(context.tasksQuery),
        fallback = fallback,
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
