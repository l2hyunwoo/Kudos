package io.github.l2hyunwoo.tasks.detail

import io.github.l2hyunwoo.data.tasks.model.TaskStatus
import io.github.l2hyunwoo.data.tasks.model.UpdateTaskRequest

sealed interface TaskDetailEvent {
    data object ShowEditSheet : TaskDetailEvent

    data object DismissEditSheet : TaskDetailEvent

    data class UpdateTask(
        val request: UpdateTaskRequest,
    ) : TaskDetailEvent

    data class ChangeStatus(
        val status: TaskStatus,
    ) : TaskDetailEvent

    data object RequestDelete : TaskDetailEvent

    data object ConfirmDelete : TaskDetailEvent

    data object UndoDelete : TaskDetailEvent

    data object NavigateBack : TaskDetailEvent

    // Subtask mutations. A subtask is a child Task carrying parentTaskId = this task's UUID.
    data class CreateSubtask(
        val title: String,
    ) : TaskDetailEvent

    data class ToggleSubtask(
        val subtask: SubtaskItem,
    ) : TaskDetailEvent

    data class DeleteSubtask(
        val subtask: SubtaskItem,
    ) : TaskDetailEvent
}
