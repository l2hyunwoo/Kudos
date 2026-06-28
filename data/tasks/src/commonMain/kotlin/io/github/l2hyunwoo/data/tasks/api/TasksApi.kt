package io.github.l2hyunwoo.data.tasks.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.PATCH
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import io.github.l2hyunwoo.data.tasks.model.CreateTaskRequest
import io.github.l2hyunwoo.data.tasks.model.ProjectTasksResponse
import io.github.l2hyunwoo.data.tasks.model.TasksResponse
import io.github.l2hyunwoo.data.tasks.model.UpdateTaskRequest

interface TasksApi {
    @GET("functions/v1/get-tasks")
    suspend fun getTasks(): TasksResponse

    @GET("functions/v1/project-tasks/{projectId}")
    suspend fun getProjectTasks(
        @Path("projectId") projectId: String,
    ): ProjectTasksResponse

    @POST("functions/v1/tasks-api/tasks")
    suspend fun createTask(
        @Body request: CreateTaskRequest,
    )

    // taskId is the text identifier (e.g. "KUDOS-1"), not the UUID `id`.
    // The Edge Function matches on ?task_id=eq.${taskId}.
    @PATCH("functions/v1/tasks-api/tasks/{taskId}")
    suspend fun updateTask(
        @Path("taskId") taskId: String,
        @Body request: UpdateTaskRequest,
    )

    @DELETE("functions/v1/tasks-api/tasks/{taskId}")
    suspend fun deleteTask(
        @Path("taskId") taskId: String,
    )
}
