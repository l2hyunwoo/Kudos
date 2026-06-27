package io.github.l2hyunwoo.tasks.detail

import androidx.compose.runtime.Immutable
import io.github.l2hyunwoo.data.tasks.model.TaskPriority
import io.github.l2hyunwoo.data.tasks.model.TaskStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class TaskDetailUiState(
    val taskId: String,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val priority: TaskPriority,
    val dueDate: String?,
    val subtasks: ImmutableList<SubtaskItem> = persistentListOf(),
    val showEditSheet: Boolean = false,
    val isMutating: Boolean = false,
    // Set while the undo snackbar is showing, before the delete is confirmed.
    val pendingDelete: Boolean = false,
    val error: Throwable? = null,
) {
    val subtaskTotal: Int get() = subtasks.size
    val subtaskDone: Int get() = subtasks.count { it.status == TaskStatus.DONE }
}

@Immutable
data class SubtaskItem(
    val id: String,
    val taskId: String,
    val title: String,
    val status: TaskStatus,
)
