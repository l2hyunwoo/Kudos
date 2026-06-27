package io.github.l2hyunwoo.data.tasks.api

import io.github.l2hyunwoo.data.tasks.model.CreateTaskRequest
import io.github.l2hyunwoo.data.tasks.model.ProjectTasksResponse
import io.github.l2hyunwoo.data.tasks.model.TasksResponse
import io.github.l2hyunwoo.data.tasks.model.UpdateTaskRequest

interface TasksApiClient {
    suspend fun getTasks(): TasksResponse

    suspend fun getProjectTasks(projectId: String): ProjectTasksResponse

    suspend fun createTask(request: CreateTaskRequest)

    suspend fun updateTask(taskId: String, request: UpdateTaskRequest)

    suspend fun deleteTask(taskId: String)
}

