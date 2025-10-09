package io.github.l2hyunwoo.data.tasks.query

import dev.zacsweers.metro.Inject
import io.github.l2hyunwoo.data.tasks.api.TasksApiClient
import io.github.l2hyunwoo.data.tasks.model.Task
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.buildQueryKey

@Inject
class DefaultProjectTasksQueryKeyFactory(
    private val apiClient: TasksApiClient,
) {
    fun create(projectId: String): QueryKey<List<Task>> = buildQueryKey(
        id = QueryId("project_tasks_query_$projectId"),
        fetch = {
            val response = apiClient.getProjectTasks(projectId)
            response.tasks
        }
    )
}
