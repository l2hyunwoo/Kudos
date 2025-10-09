package io.github.l2hyunwoo.data.tasks.api

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import io.github.l2hyunwoo.data.tasks.model.ProjectTasksResponse
import io.github.l2hyunwoo.data.tasks.model.TasksResponse

interface TasksApi {
    @GET("functions/v1/get-tasks")
    suspend fun getTasks(): TasksResponse

    @GET("functions/v1/project-tasks/{projectId}")
    suspend fun getProjectTasks(@Path("projectId") projectId: String): ProjectTasksResponse
}
