package io.github.l2hyunwoo.tasks

import io.github.l2hyunwoo.data.tasks.model.CreateTaskRequest
import io.github.l2hyunwoo.data.tasks.model.TaskPriority
import io.github.l2hyunwoo.data.tasks.model.TaskStatus

sealed interface TaskListEvent {
    data class CreateTask(
        val request: CreateTaskRequest,
    ) : TaskListEvent

    // taskId ("KUDOS-1") drives the PATCH path; id (UUID) locates the row in the cached list.
    data class ChangeStatus(
        val taskId: String,
        val id: String,
        val status: TaskStatus,
    ) : TaskListEvent

    // Drag-and-drop reorder mapped to a priority change: dropping a task next to a different-grade
    // neighbor re-grades it. taskId drives the PATCH path; id locates the row for the optimistic
    // override; priority is the destination grade.
    data class ReorderPriority(
        val taskId: String,
        val id: String,
        val priority: TaskPriority,
    ) : TaskListEvent

    data class DeleteTask(
        val taskId: String,
        val id: String,
    ) : TaskListEvent

    // Swipe-delete shows an undo snackbar before committing; these resolve that pending state.
    data object UndoDelete : TaskListEvent

    data object ConfirmDelete : TaskListEvent
}
