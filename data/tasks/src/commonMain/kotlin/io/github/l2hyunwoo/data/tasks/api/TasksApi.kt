package io.github.l2hyunwoo.data.tasks.api

import de.jensklingenberg.ktorfit.http.GET
import io.github.l2hyunwoo.data.tasks.model.TasksResponse

interface TasksApi {
    @GET("functions/v1/get-tasks")
    suspend fun getTasks(): TasksResponse
}
