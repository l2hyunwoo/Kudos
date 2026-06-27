package io.github.l2hyunwoo.data.tasks.query

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.l2hyunwoo.data.tasks.api.TasksApiClient
import io.github.l2hyunwoo.data.tasks.cache.TasksCacheDataStore
import io.github.l2hyunwoo.data.tasks.model.TasksResponse
import io.github.l2hyunwoo.kudos.core.common.DataScope
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.QueryPreloadData
import soil.query.buildQueryKey

// Single source of truth for the tasks query id, shared with the mutation keys so their
// onMutateEffect invalidations target exactly this query (and the type argument matches).
val TasksQueryId = QueryId<List<TasksResponse.CategoryWithTasks>>("tasks_query")

@ContributesBinding(DataScope::class)
@Inject
class DefaultTasksQueryKey(
    private val apiClient: TasksApiClient,
    private val dataStore: TasksCacheDataStore,
) : QueryKey<List<TasksResponse.CategoryWithTasks>> by buildQueryKey(
    id = TasksQueryId,
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
