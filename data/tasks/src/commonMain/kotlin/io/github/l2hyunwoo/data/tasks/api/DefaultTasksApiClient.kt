package io.github.l2hyunwoo.data.tasks.api

import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.l2hyunwoo.data.tasks.model.CreateTaskRequest
import io.github.l2hyunwoo.data.tasks.model.ProjectTasksResponse
import io.github.l2hyunwoo.data.tasks.model.TasksResponse
import io.github.l2hyunwoo.kudos.core.common.DataScope

@ContributesBinding(DataScope::class)
@Inject
class DefaultTasksApiClient internal constructor(ktorfit: Ktorfit) : TasksApiClient {
    val tasksApi = ktorfit.createTasksApi()

    override suspend fun getTasks(): TasksResponse {
        return tasksApi.getTasks()
    }

    override suspend fun getProjectTasks(projectId: String): ProjectTasksResponse {
        return tasksApi.getProjectTasks(projectId)
    }

    override suspend fun createTask(request: CreateTaskRequest) {
        return tasksApi.createTask(request)
    }
}
