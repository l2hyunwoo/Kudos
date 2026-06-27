package io.github.l2hyunwoo.tasks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.l2hyunwoo.data.tasks.model.DeleteTaskParams
import io.github.l2hyunwoo.data.tasks.model.TasksResponse
import io.github.l2hyunwoo.data.tasks.model.UpdateTaskParams
import io.github.l2hyunwoo.data.tasks.model.UpdateTaskRequest
import io.github.l2hyunwoo.kudos.core.common.compose.EventEffect
import io.github.l2hyunwoo.kudos.core.common.compose.EventFlow
import kotlinx.collections.immutable.toImmutableList
import soil.query.compose.rememberMutation

@Composable
context(context: TasksContext)
fun taskListPresenter(
    eventFlow: EventFlow<TaskListEvent>,
    categories: List<TasksResponse.CategoryWithTasks>,
    searchQuery: String = "",
): TaskListUiState {
    val createTaskMutation = rememberMutation(context.createTaskMutation)
    val updateTaskMutation = rememberMutation(context.updateTaskMutation)
    val deleteTaskMutation = rememberMutation(context.deleteTaskMutation)

    // Swipe-delete is deferred behind an undo snackbar: the row is hidden locally while pending and
    // only committed (mutation) on ConfirmDelete, restored on UndoDelete. Mirrors TaskDetail.
    var pendingDelete by remember { mutableStateOf<PendingDelete?>(null) }

    EventEffect(eventFlow) { event ->
        when (event) {
            is TaskListEvent.CreateTask -> {
                createTaskMutation.mutate(event.request)
            }

            is TaskListEvent.ChangeStatus -> {
                // Status-only PATCH: cache invalidation in the mutation refreshes the list.
                updateTaskMutation.mutate(
                    UpdateTaskParams(
                        taskId = event.taskId,
                        request = UpdateTaskRequest(status = event.status),
                    )
                )
            }

            is TaskListEvent.DeleteTask -> {
                pendingDelete = PendingDelete(taskId = event.taskId, id = event.id)
            }

            is TaskListEvent.UndoDelete -> {
                pendingDelete = null
            }

            is TaskListEvent.ConfirmDelete -> {
                pendingDelete?.let { pending ->
                    deleteTaskMutation.mutate(DeleteTaskParams(taskId = pending.taskId, id = pending.id))
                }
                pendingDelete = null
            }
        }
    }

    val query = searchQuery.trim()
    val pendingId = pendingDelete?.id
    val visibleCategories = categories.mapNotNull { category ->
        val matches = category.tasks.filter { task ->
            // Hide the row that is pending deletion so the list reads as "already deleted" under undo.
            if (task.id == pendingId) return@filter false
            if (query.isEmpty()) return@filter true
            // Substring match on title/description; categories with no match drop out so the screen's
            // empty state ("결과 없음") shows when nothing matches.
            task.title.contains(query, ignoreCase = true) ||
                task.description?.contains(query, ignoreCase = true) == true
        }
        // Keep empty categories when not searching (the screen renders headers itself); drop them
        // under an active query so "결과 없음" shows when nothing matches.
        when {
            matches.isNotEmpty() -> category.copy(tasks = matches)
            query.isEmpty() -> category.copy(tasks = matches)
            else -> null
        }
    }

    // Group the SAME filtered task set so search + time-grouping compose: flatten the surviving
    // tasks across categories, then bucket by due date relative to today. Recomputed only when the
    // filtered set changes (todayIso is stable across a composition / day).
    val filteredTasks = visibleCategories.flatMap { it.tasks }
    val groups = remember(filteredTasks) {
        groupTasksByDueDate(filteredTasks, todayIso())
    }

    return TaskListUiState(
        categories = visibleCategories.toImmutableList(),
        groups = groups,
        isLoading = createTaskMutation.isPending ||
            updateTaskMutation.isPending ||
            deleteTaskMutation.isPending,
        error = createTaskMutation.error
            ?: updateTaskMutation.error
            ?: deleteTaskMutation.error,
        searchQuery = query,
        pendingDelete = pendingDelete,
    )
}
