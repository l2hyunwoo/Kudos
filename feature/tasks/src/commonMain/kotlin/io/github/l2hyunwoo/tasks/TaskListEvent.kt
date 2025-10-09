package io.github.l2hyunwoo.tasks

import io.github.l2hyunwoo.data.tasks.model.CreateTaskRequest

sealed interface TaskListEvent {
    data class CreateTask(val request: CreateTaskRequest) : TaskListEvent
}
