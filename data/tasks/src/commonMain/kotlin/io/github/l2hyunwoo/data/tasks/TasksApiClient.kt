package io.github.l2hyunwoo.data.tasks

import io.github.l2hyunwoo.data.tasks.model.TasksResponse

interface TasksApiClient {
    suspend fun getTasks(): TasksResponse
}

