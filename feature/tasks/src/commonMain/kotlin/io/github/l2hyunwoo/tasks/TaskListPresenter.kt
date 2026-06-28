package io.github.l2hyunwoo.tasks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.l2hyunwoo.data.tasks.model.DeleteTaskParams
import io.github.l2hyunwoo.data.tasks.model.TaskPriority
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

    // Optimistic priority overrides from drag-and-drop reorder, keyed by task `id`. Applied to the
    // task set before grouping so the row re-sorts/animates to its new grade immediately, then
    // committed via updateTaskMutation. On commit failure the override is dropped (rollback) and the
    // mutation surfaces the error. On success it is reconciled away once the refetched list catches up
    // (see below), so a later Edit-Task priority change can't be clobbered by a stale override.
    var priorityOverrides by remember { mutableStateOf<Map<String, TaskPriority>>(emptyMap()) }

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
                    ),
                )
            }

            is TaskListEvent.ReorderPriority -> {
                // Optimistic: show the new grade immediately, then commit. mutate() throws on failure
                // (it suspends through the PATCH), so a failed commit rolls the override back and the
                // mutation's own error state surfaces the snackbar.
                priorityOverrides = priorityOverrides + (event.id to event.priority)
                try {
                    updateTaskMutation.mutate(
                        UpdateTaskParams(
                            taskId = event.taskId,
                            request = UpdateTaskRequest(priority = event.priority),
                        ),
                    )
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    priorityOverrides = priorityOverrides - event.id
                }
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
    val visibleCategories =
        categories.mapNotNull { category ->
            val matches =
                category.tasks.filter { task ->
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
    // tasks across categories, then bucket by due date relative to today.
    val baseTasks = visibleCategories.flatMap { it.tasks }
    // Reconcile overrides the refetched list already reflects, so a stale override can't later clobber
    // an independent (e.g. Edit-Task) priority change. Done in an effect (not the body) to avoid a
    // backward write to snapshot state during composition.
    LaunchedEffect(baseTasks, priorityOverrides) {
        if (priorityOverrides.isEmpty()) return@LaunchedEffect
        val settled = baseTasks.filter { priorityOverrides[it.id] == it.priority }.map { it.id }
        if (settled.isNotEmpty()) priorityOverrides = priorityOverrides - settled.toSet()
    }
    // Apply the optimistic overrides for rendering. Keep id stable through the copy so animateItem
    // animates the move instead of a remove+insert.
    val filteredTasks =
        if (priorityOverrides.isEmpty()) {
            baseTasks
        } else {
            baseTasks.map { task ->
                priorityOverrides[task.id]?.let { task.copy(priority = it) } ?: task
            }
        }
    val groups =
        remember(filteredTasks) {
            groupTasksByDueDate(filteredTasks, todayIso())
        }

    return TaskListUiState(
        categories = visibleCategories.toImmutableList(),
        groups = groups,
        isLoading =
            createTaskMutation.isPending ||
                updateTaskMutation.isPending ||
                deleteTaskMutation.isPending,
        error =
            createTaskMutation.error
                ?: updateTaskMutation.error
                ?: deleteTaskMutation.error,
        searchQuery = query,
        pendingDelete = pendingDelete,
    )
}
