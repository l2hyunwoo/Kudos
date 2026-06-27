package io.github.l2hyunwoo.tasks.dashboard

import androidx.compose.runtime.Immutable
import io.github.l2hyunwoo.data.tasks.model.Task
import io.github.l2hyunwoo.data.tasks.model.TaskPriority
import io.github.l2hyunwoo.data.tasks.model.TaskStatus
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableMap

@Immutable
data class DashboardUiState(
    val completionRatio: Float,
    val statusCounts: ImmutableMap<TaskStatus, Int>,
    val priorityCounts: ImmutableMap<TaskPriority, Int>,
    val totalCount: Int,
) {
    val doneCount: Int get() = statusCounts[TaskStatus.DONE] ?: 0
}

// Pure aggregation over a flat task list, kept out of the presenter so it is unit-testable without a
// Compose runtime. Always returns every enum key (count 0 when absent) so the widgets render a fixed
// set of bars regardless of which statuses/priorities the data happens to contain.
fun aggregate(tasks: List<Task>): DashboardUiState {
    if (tasks.isEmpty()) return EmptyDashboard

    val statusCounts = TaskStatus.entries.associateWith { status ->
        tasks.count { it.status == status }
    }
    val priorityCounts = TaskPriority.entries.associateWith { priority ->
        tasks.count { it.priority == priority }
    }
    val doneCount = statusCounts[TaskStatus.DONE] ?: 0
    val completionRatio = doneCount.toFloat() / tasks.size.toFloat()

    return DashboardUiState(
        completionRatio = completionRatio,
        statusCounts = statusCounts.toImmutableMap(),
        priorityCounts = priorityCounts.toImmutableMap(),
        totalCount = tasks.size,
    )
}

// Zero state: every enum key present at 0 so the empty-case still has a well-formed shape.
private val EmptyDashboard = DashboardUiState(
    completionRatio = 0f,
    statusCounts = persistentMapOf(*TaskStatus.entries.map { it to 0 }.toTypedArray()),
    priorityCounts = persistentMapOf(*TaskPriority.entries.map { it to 0 }.toTypedArray()),
    totalCount = 0,
)
