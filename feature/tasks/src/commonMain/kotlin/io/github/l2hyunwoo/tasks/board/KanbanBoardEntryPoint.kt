package io.github.l2hyunwoo.tasks.board

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.data.tasks.model.Task
import io.github.l2hyunwoo.kudos.core.common.compose.EventFlow
import io.github.l2hyunwoo.kudos.core.common.compose.rememberEventFlow
import io.github.l2hyunwoo.kudos.core.soil.SoilBoundary
import io.github.l2hyunwoo.kudos.core.soil.SoilFallbackDefaults
import io.github.l2hyunwoo.tasks.TaskListEvent
import io.github.l2hyunwoo.tasks.TasksContext
import soil.query.compose.rememberQuery

// Board view over the SAME tasksQuery the list reads (no new Context/Scope/query). Embedded-style: no
// own chrome (MainScreen owns glass header/nav), chrome-less fallback, accepts [topContentPadding].
// Reuses the shared tasks eventFlow: drops emit TaskListEvent.ChangeStatus, which the kanbanPresenter
// commits optimistically (statusOverrides) the same way taskListPresenter handles priority reorders.
@Composable
context(context: TasksContext)
fun KanbanBoardEntryPoint(
    eventFlow: EventFlow<TaskListEvent>? = null,
    onNavigateToTaskDetail: (Task) -> Unit = {},
    searchQuery: String = "",
    topContentPadding: Dp = 0.dp,
) {
    val actualEventFlow = eventFlow ?: rememberEventFlow()

    SoilBoundary(
        state = rememberQuery(context.tasksQuery),
        fallback = SoilFallbackDefaults.default(),
    ) { categories ->
        val uiState = kanbanPresenter(
            eventFlow = actualEventFlow,
            categories = categories,
            searchQuery = searchQuery,
        )
        KanbanBoard(
            uiState = uiState,
            eventFlow = actualEventFlow,
            topContentPadding = topContentPadding,
            onTaskClick = onNavigateToTaskDetail,
        )
    }
}
