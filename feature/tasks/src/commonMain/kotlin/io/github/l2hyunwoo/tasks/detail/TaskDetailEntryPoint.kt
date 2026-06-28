package io.github.l2hyunwoo.tasks.detail

import androidx.compose.runtime.Composable
import io.github.l2hyunwoo.data.tasks.model.TaskPriority
import io.github.l2hyunwoo.data.tasks.model.TaskStatus
import io.github.l2hyunwoo.kudos.core.common.compose.rememberEventFlow

@Composable
context(context: TaskDetailContext)
fun TaskDetailEntryPoint(
    id: String,
    taskId: String,
    title: String,
    description: String?,
    status: TaskStatus,
    priority: TaskPriority,
    dueDate: String?,
    onNavigateBack: () -> Unit
) {
    val eventFlow = rememberEventFlow<TaskDetailEvent>()

    val uiState = taskDetailPresenter(
        id = id,
        taskId = taskId,
        initialTitle = title,
        initialDescription = description,
        initialStatus = status,
        initialPriority = priority,
        initialDueDate = dueDate,
        eventFlow = eventFlow,
        onNavigateBack = onNavigateBack
    )

    TaskDetailScreen(
        uiState = uiState,
        eventFlow = eventFlow,
        // The route UUID is the shared-element identity; it matches the list row's task.id so the
        // moon/title/id morph across the navigation. Not part of uiState (it's navigation identity,
        // not presented state).
        sharedKeyId = id,
    )
}
