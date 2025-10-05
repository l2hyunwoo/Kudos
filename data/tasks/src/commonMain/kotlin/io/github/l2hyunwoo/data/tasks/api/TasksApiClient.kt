package io.github.l2hyunwoo.data.tasks.api

import io.github.l2hyunwoo.data.tasks.model.TasksResponse

interface TasksApiClient {
    suspend fun getTasks(): TasksResponse
}

