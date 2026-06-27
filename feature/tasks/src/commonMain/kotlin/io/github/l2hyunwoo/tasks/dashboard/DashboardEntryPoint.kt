package io.github.l2hyunwoo.tasks.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.kudos.core.soil.SoilBoundary
import io.github.l2hyunwoo.kudos.core.soil.SoilFallbackDefaults
import io.github.l2hyunwoo.tasks.TasksContext
import soil.query.compose.rememberQuery

// Reuses the SAME tasksQuery the task list reads (no new Context/Scope/query). Embedded-style: no own
// chrome — MainScreen owns the glass header/nav, so the loading/error fallback is chrome-less and the
// screen accepts a [topContentPadding] to clear the header.
@Composable
context(context: TasksContext)
fun DashboardEntryPoint(
    topContentPadding: Dp = 0.dp,
) {
    SoilBoundary(
        state = rememberQuery(context.tasksQuery),
        fallback = SoilFallbackDefaults.default(),
    ) { categories ->
        val uiState = dashboardPresenter(categories = categories)
        DashboardScreen(
            uiState = uiState,
            topContentPadding = topContentPadding,
        )
    }
}
