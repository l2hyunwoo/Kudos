package io.github.l2hyunwoo.tasks.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.l2hyunwoo.data.tasks.model.CreateTaskRequest
import io.github.l2hyunwoo.data.tasks.model.DeleteTaskParams
import io.github.l2hyunwoo.data.tasks.model.TaskPriority
import io.github.l2hyunwoo.data.tasks.model.TaskStatus
import io.github.l2hyunwoo.data.tasks.model.UpdateTaskParams
import io.github.l2hyunwoo.data.tasks.model.UpdateTaskRequest
import io.github.l2hyunwoo.kudos.core.common.compose.EventEffect
import io.github.l2hyunwoo.kudos.core.common.compose.EventFlow
import kotlinx.collections.immutable.toImmutableList
import soil.query.compose.rememberMutation
import soil.query.compose.rememberQuery

@Composable
context(context: TaskDetailContext)
fun taskDetailPresenter(
    id: String,
    taskId: String,
    initialTitle: String,
    initialDescription: String?,
    initialStatus: TaskStatus,
    initialPriority: TaskPriority,
    initialDueDate: String?,
    eventFlow: EventFlow<TaskDetailEvent>,
    onNavigateBack: () -> Unit,
): TaskDetailUiState {
    val createTaskMutation = rememberMutation(context.createTaskMutation)
    val updateTaskMutation = rememberMutation(context.updateTaskMutation)
    val deleteTaskMutation = rememberMutation(context.deleteTaskMutation)

    // Subtasks are children in the same cached list, matched by parent_task_id == this task's UUID.
    val tasksQuery = rememberQuery(context.tasksQuery)
    val subtasks = remember(tasksQuery.data, id) {
        tasksQuery.data
            .orEmpty()
            .asSequence()
            .flatMap { it.tasks.asSequence() }
            .filter { it.parentTaskId == id }
            .map { SubtaskItem(id = it.id, taskId = it.taskId, title = it.title, status = it.status) }
            .toList()
            .toImmutableList()
    }

    // A new subtask inherits the parent's category (required by the create API) and project, looked
    // up from the same cached list by the parent task's UUID.
    val parentTask = remember(tasksQuery.data, id) {
        tasksQuery.data.orEmpty().firstNotNullOfOrNull { category ->
            category.tasks.firstOrNull { it.id == id }?.let { task -> category.id to task }
        }
    }
    val parentCategoryId = parentTask?.first
    val parentProjectId = parentTask?.second?.projectId

    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var status by remember { mutableStateOf(initialStatus) }
    var priority by remember { mutableStateOf(initialPriority) }
    var dueDate by remember { mutableStateOf(initialDueDate) }
    var showEditSheet by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf(false) }

    EventEffect(eventFlow) { event ->
        when (event) {
            is TaskDetailEvent.ShowEditSheet -> {
                showEditSheet = true
            }

            is TaskDetailEvent.DismissEditSheet -> {
                showEditSheet = false
            }

            is TaskDetailEvent.UpdateTask -> {
                // Optimistic local reflection; cache is invalidated by the mutation.
                event.request.title?.let { title = it }
                description = event.request.description
                event.request.status?.let { status = it }
                event.request.priority?.let { priority = it }
                dueDate = event.request.dueDate
                updateTaskMutation.mutate(UpdateTaskParams(taskId = taskId, request = event.request))
                showEditSheet = false
            }

            is TaskDetailEvent.ChangeStatus -> {
                status = event.status
                // Status-only PATCH: only `status` is set, every other field stays null/absent.
                updateTaskMutation.mutate(
                    UpdateTaskParams(
                        taskId = taskId,
                        request = UpdateTaskRequest(status = event.status)
                    )
                )
            }

            is TaskDetailEvent.RequestDelete -> {
                pendingDelete = true
            }

            is TaskDetailEvent.UndoDelete -> {
                pendingDelete = false
            }

            is TaskDetailEvent.ConfirmDelete -> {
                pendingDelete = false
                deleteTaskMutation.mutate(DeleteTaskParams(taskId = taskId, id = id))
                onNavigateBack()
            }

            is TaskDetailEvent.NavigateBack -> {
                onNavigateBack()
            }

            is TaskDetailEvent.CreateSubtask -> {
                // categoryId is required by the create API; skip if the parent isn't in the cache yet.
                parentCategoryId?.let { categoryId ->
                    createTaskMutation.mutate(
                        CreateTaskRequest(
                            categoryId = categoryId,
                            title = event.title,
                            projectId = parentProjectId,
                            status = TaskStatus.TODO,
                            priority = TaskPriority.MEDIUM,
                            parentTaskId = id,
                        )
                    )
                }
            }

            is TaskDetailEvent.ToggleSubtask -> {
                val nextStatus = if (event.subtask.status == TaskStatus.DONE) {
                    TaskStatus.TODO
                } else {
                    TaskStatus.DONE
                }
                updateTaskMutation.mutate(
                    UpdateTaskParams(
                        taskId = event.subtask.taskId,
                        request = UpdateTaskRequest(status = nextStatus),
                    )
                )
            }

            is TaskDetailEvent.DeleteSubtask -> {
                deleteTaskMutation.mutate(
                    DeleteTaskParams(taskId = event.subtask.taskId, id = event.subtask.id),
                )
            }
        }
    }

    return TaskDetailUiState(
        taskId = taskId,
        title = title,
        description = description,
        status = status,
        priority = priority,
        dueDate = dueDate,
        subtasks = subtasks,
        showEditSheet = showEditSheet,
        isMutating = updateTaskMutation.isPending || deleteTaskMutation.isPending,
        pendingDelete = pendingDelete,
        error = updateTaskMutation.error ?: deleteTaskMutation.error,
    )
}
