package io.github.l2hyunwoo.data.tasks.model

import soil.query.MutationKey

typealias CreateTaskMutationKey = MutationKey<Unit, CreateTaskRequest>
typealias UpdateTaskMutationKey = MutationKey<Unit, UpdateTaskParams>
typealias DeleteTaskMutationKey = MutationKey<List<TasksResponse.CategoryWithTasks>, DeleteTaskParams>

data class UpdateTaskParams(
    // task_id text identifier (e.g. "KUDOS-1"), used as the PATCH path param.
    val taskId: String,
    val request: UpdateTaskRequest,
)

data class DeleteTaskParams(
    // task_id text identifier (e.g. "KUDOS-1"), used as the DELETE path param.
    val taskId: String,
    // UUID `id`, used to locate the task inside the cached list for optimistic removal.
    val id: String,
)
