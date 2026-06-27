package io.github.l2hyunwoo.tasks

import androidx.compose.runtime.Immutable
import io.github.l2hyunwoo.data.tasks.model.TasksResponse
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class TaskListUiState(
    val categories: ImmutableList<TasksResponse.CategoryWithTasks> = persistentListOf(),
    val isLoading: Boolean = false,
    val error: Throwable? = null,
    val searchQuery: String = "",
    // Set while the undo snackbar is showing for a swipe-deleted row, before the delete commits.
    val pendingDelete: PendingDelete? = null,
)

@Immutable
data class PendingDelete(val taskId: String, val id: String)
