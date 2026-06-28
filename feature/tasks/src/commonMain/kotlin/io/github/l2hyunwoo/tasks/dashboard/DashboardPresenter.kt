package io.github.l2hyunwoo.tasks.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.github.l2hyunwoo.data.tasks.model.TasksResponse

// Read-only screen: flatten categories to a single task set and aggregate. No events (Dashboard is a
// summary), so there is no eventFlow or mutation wiring here. remember(categories) keeps the pure
// aggregate off the recomposition hot path.
@Composable
fun dashboardPresenter(categories: List<TasksResponse.CategoryWithTasks>): DashboardUiState =
    remember(categories) {
        aggregate(categories.flatMap { it.tasks })
    }
