package io.github.l2hyunwoo.data.tasks.query

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.l2hyunwoo.data.tasks.api.TasksApiClient
import io.github.l2hyunwoo.data.tasks.cache.TasksCacheDataStore
import io.github.l2hyunwoo.data.tasks.model.TasksResponse
import io.github.l2hyunwoo.kudos.core.network.di.NetworkScope
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.QueryPreloadData
import soil.query.buildQueryKey

@ContributesBinding(NetworkScope::class)
@Inject
class DefaultTasksQueryKey(
    private val apiClient: TasksApiClient,
    private val dataStore: TasksCacheDataStore,
) : QueryKey<List<TasksResponse.CategoryWithTasks>> by buildQueryKey(
    id = QueryId("tasks_query"),
    fetch = {
        val response = apiClient.getTasks()
        dataStore.save(response)
        response.categories
    }
) {
    override fun onPreloadData(): QueryPreloadData<List<TasksResponse.CategoryWithTasks>>? {
        return { dataStore.getCache()?.categories }
    }
}
