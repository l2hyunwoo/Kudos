package io.github.l2hyunwoo.tasks.board

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.l2hyunwoo.data.tasks.model.Task
import io.github.l2hyunwoo.data.tasks.model.TaskStatus
import io.github.l2hyunwoo.data.tasks.model.TasksResponse
import io.github.l2hyunwoo.data.tasks.model.UpdateTaskParams
import io.github.l2hyunwoo.data.tasks.model.UpdateTaskRequest
import io.github.l2hyunwoo.kudos.core.common.compose.EventEffect
import io.github.l2hyunwoo.kudos.core.common.compose.EventFlow
import io.github.l2hyunwoo.tasks.TaskListEvent
import io.github.l2hyunwoo.tasks.TasksContext
import kotlinx.collections.immutable.toImmutableList
import soil.query.compose.rememberMutation

// Fixed left-to-right column order.
private val ColumnOrder = listOf(
    TaskStatus.BACKLOG,
    TaskStatus.TODO,
    TaskStatus.IN_PROGRESS,
    TaskStatus.DONE,
)

// Pure: bucket a flat task list into the four kanban columns in [ColumnOrder]. Empty columns are kept
// so the board always renders all four lanes. Within a column, tasks keep input order.
fun bucketByStatus(tasks: List<Task>): List<KanbanColumn> {
    val byStatus = tasks.groupBy { it.status }
    return ColumnOrder.map { status ->
        KanbanColumn(
            status = status,
            tasks = (byStatus[status] ?: emptyList()).toImmutableList(),
        )
    }
}

@Composable
context(context: TasksContext)
fun kanbanPresenter(
    eventFlow: EventFlow<TaskListEvent>,
    categories: List<TasksResponse.CategoryWithTasks>,
    searchQuery: String = "",
): KanbanUiState {
    val updateTaskMutation = rememberMutation(context.updateTaskMutation)

    // Optimistic status overrides from a board drop, keyed by task `id`. Applied to the task set
    // before bucketing so the card moves columns immediately, then committed via updateTaskMutation.
    // On commit failure the override is dropped (rollback) and the mutation surfaces the error. On
    // success it is reconciled away once the refetched list catches up (see below). Mirrors the
    // priorityOverrides pattern in taskListPresenter exactly, but for status instead of priority.
    var statusOverrides by remember { mutableStateOf<Map<String, TaskStatus>>(emptyMap()) }

    EventEffect(eventFlow) { event ->
        when (event) {
            is TaskListEvent.ChangeStatus -> {
                // Optimistic: show the new column immediately, then commit. mutate() suspends through
                // the PATCH and throws on failure, so a failed commit rolls the override back and the
                // mutation's own error state surfaces.
                statusOverrides = statusOverrides + (event.id to event.status)
                try {
                    updateTaskMutation.mutate(
                        UpdateTaskParams(
                            taskId = event.taskId,
                            request = UpdateTaskRequest(status = event.status),
                        )
                    )
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    statusOverrides = statusOverrides - event.id
                }
            }

            // The board only emits ChangeStatus; other TaskListEvents are owned by the list presenter
            // and ignored here so a shared eventFlow stays safe.
            else -> Unit
        }
    }

    val query = searchQuery.trim()
    val baseTasks = categories.flatMap { it.tasks }.filter { task ->
        if (query.isEmpty()) return@filter true
        task.title.contains(query, ignoreCase = true) ||
            task.description?.contains(query, ignoreCase = true) == true
    }

    // Reconcile overrides the refetched list already reflects, so a stale override can't later clobber
    // an independent status change. Done in an effect (not the body) to avoid a backward write to
    // snapshot state during composition. Mirrors taskListPresenter.
    LaunchedEffect(baseTasks, statusOverrides) {
        if (statusOverrides.isEmpty()) return@LaunchedEffect
        val settled = baseTasks.filter { statusOverrides[it.id] == it.status }.map { it.id }
        if (settled.isNotEmpty()) statusOverrides = statusOverrides - settled.toSet()
    }

    // Apply the optimistic overrides for rendering. Keep id stable through the copy so the card reads
    // as moved (not removed+inserted).
    val effectiveTasks = if (statusOverrides.isEmpty()) {
        baseTasks
    } else {
        baseTasks.map { task ->
            statusOverrides[task.id]?.let { task.copy(status = it) } ?: task
        }
    }

    val columns = remember(effectiveTasks) {
        bucketByStatus(effectiveTasks).toImmutableList()
    }

    return KanbanUiState(
        columns = columns,
        isLoading = updateTaskMutation.isPending,
        error = updateTaskMutation.error,
    )
}
