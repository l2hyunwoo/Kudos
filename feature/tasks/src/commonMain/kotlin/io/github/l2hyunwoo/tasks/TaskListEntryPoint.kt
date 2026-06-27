package io.github.l2hyunwoo.tasks

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.skydoves.cloudy.Sky
import com.skydoves.cloudy.rememberSky
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
    searchQuery: String = "",
    // Reuse the parent's backdrop recorder when given (MainScreen tab); standalone routes create one.
    sky: Sky = rememberSky(),
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
            categories = uiState.categories,
            searchQuery = uiState.searchQuery,
            sky = sky,
        )
    }
}
