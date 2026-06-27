package io.github.l2hyunwoo.tasks

import io.github.l2hyunwoo.data.tasks.model.CreateTaskRequest
import io.github.l2hyunwoo.data.tasks.model.TaskStatus

sealed interface TaskListEvent {
    data class CreateTask(val request: CreateTaskRequest) : TaskListEvent

    // taskId ("KUDOS-1") drives the PATCH path; id (UUID) locates the row in the cached list.
    data class ChangeStatus(
        val taskId: String,
        val id: String,
        val status: TaskStatus,
    ) : TaskListEvent

    data class DeleteTask(val taskId: String, val id: String) : TaskListEvent

    // Swipe-delete shows an undo snackbar before committing; these resolve that pending state.
    data object UndoDelete : TaskListEvent
    data object ConfirmDelete : TaskListEvent
}
