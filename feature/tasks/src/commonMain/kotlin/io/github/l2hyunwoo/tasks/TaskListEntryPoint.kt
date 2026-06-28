package io.github.l2hyunwoo.tasks

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.data.tasks.model.Task
import io.github.l2hyunwoo.data.tasks.model.TasksResponse
import io.github.l2hyunwoo.kudos.core.common.compose.EventFlow
import io.github.l2hyunwoo.kudos.core.common.compose.rememberEventFlow
import io.github.l2hyunwoo.kudos.core.soil.SoilBoundary
import io.github.l2hyunwoo.kudos.core.soil.SoilFallbackDefaults
import kotlinx.coroutines.launch
import soil.query.compose.QueryObject
import soil.query.compose.rememberQuery

@OptIn(ExperimentalMaterial3Api::class)
@Composable
context(context: TasksContext)
fun TaskListEntryPoint(
    modifier: Modifier = Modifier,
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
    // Pull-to-refresh state owned by the parent (MainScreen) so the visible indicator can be drawn as
    // a sibling OUTSIDE the backdrop recorder (crisp over the glass). The gesture itself attaches to
    // the list here. Null disables PTR (standalone usage). onRefreshingChange reports validation
    // state back up so the parent's indicator tracks it.
    pullToRefreshState: PullToRefreshState? = null,
    onRefreshingChange: (Boolean) -> Unit = {},
) {
    val actualEventFlow = eventFlow ?: rememberEventFlow()
    val scope = rememberCoroutineScope()

    // Hoist the query so both SoilBoundary (loading/error) and the pull-to-refresh wiring read the same
    // instance. isValidating drives the indicator; refresh() is the suspend pull action.
    val query: QueryObject<List<TasksResponse.CategoryWithTasks>> = rememberQuery(context.tasksQuery)
    val isRefreshing = query.isValidating
    // rememberUpdatedState so the effect always calls the latest lambda without restarting when it changes.
    val currentOnRefreshingChange = rememberUpdatedState(onRefreshingChange)
    LaunchedEffect(isRefreshing) { currentOnRefreshingChange.value(isRefreshing) }

    val fallback =
        if (embedded) {
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
                },
            )
        }

    SoilBoundary(
        state = query,
        modifier = modifier,
        fallback = fallback,
    ) { categories ->
        val uiState =
            taskListPresenter(
                eventFlow = actualEventFlow,
                categories = categories,
                searchQuery = searchQuery,
            )

        TaskListScreen(
            uiState = uiState,
            eventFlow = actualEventFlow,
            topContentPadding = topContentPadding,
            onTaskClick = onNavigateToTaskDetail,
            pullToRefreshState = pullToRefreshState,
            isRefreshing = isRefreshing,
            onRefresh = { scope.launch { query.refresh() } },
        )
    }
}
