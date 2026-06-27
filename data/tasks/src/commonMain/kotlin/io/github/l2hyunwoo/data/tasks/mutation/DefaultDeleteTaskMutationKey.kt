package io.github.l2hyunwoo.data.tasks.mutation

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.l2hyunwoo.data.tasks.api.TasksApiClient
import io.github.l2hyunwoo.data.tasks.cache.TasksCacheDataStore
import io.github.l2hyunwoo.data.tasks.model.DeleteTaskMutationKey
import io.github.l2hyunwoo.data.tasks.model.DeleteTaskParams
import io.github.l2hyunwoo.data.tasks.model.TasksResponse
import io.github.l2hyunwoo.data.tasks.query.TasksQueryId
import io.github.l2hyunwoo.kudos.core.common.DataScope
import soil.query.MutationId
import soil.query.buildMutationKey
import soil.query.core.Effect
import soil.query.queryClient

@ContributesBinding(DataScope::class)
@Inject
class DefaultDeleteTaskMutationKey(
    private val apiClient: TasksApiClient,
    private val cacheDataStore: TasksCacheDataStore,
) : DeleteTaskMutationKey by buildMutationKey(
    id = MutationId("delete_task"),
    mutate = { params ->
        val previousCache = cacheDataStore.getCacheSync()

        // Optimistic update: drop the task (matched by UUID id) from its category.
        val optimisticCategories = previousCache?.categories?.map { category ->
            category.copy(tasks = category.tasks.filterNot { it.id == params.id })
        } ?: emptyList()
        cacheDataStore.save(TasksResponse(categories = optimisticCategories))

        try {
            // DELETE matches on task_id and returns {success:true} only.
            apiClient.deleteTask(params.taskId)
            // Invalidate so the query refetches the canonical list.
            cacheDataStore.clear()
            optimisticCategories
        } catch (e: Exception) {
            previousCache?.let { cacheDataStore.save(it) }
            throw e
        }
    }
) {
    // The optimistic DataStore write/clear above does not touch Soil's in-memory query cache.
    // Invalidate tasks_query so active rememberQuery subscribers refetch the canonical list.
    override fun onMutateEffect(
        variable: DeleteTaskParams,
        data: List<TasksResponse.CategoryWithTasks>,
    ): Effect = {
        queryClient.invalidateQueriesBy(TasksQueryId)
    }
}
