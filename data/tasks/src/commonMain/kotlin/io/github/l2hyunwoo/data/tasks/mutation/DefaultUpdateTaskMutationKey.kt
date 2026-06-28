package io.github.l2hyunwoo.data.tasks.mutation

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.l2hyunwoo.data.tasks.api.TasksApiClient
import io.github.l2hyunwoo.data.tasks.cache.TasksCacheDataStore
import io.github.l2hyunwoo.data.tasks.model.UpdateTaskMutationKey
import io.github.l2hyunwoo.data.tasks.model.UpdateTaskParams
import io.github.l2hyunwoo.data.tasks.query.TasksQueryId
import io.github.l2hyunwoo.kudos.core.common.DataScope
import soil.query.MutationId
import soil.query.buildMutationKey
import soil.query.core.Effect
import soil.query.queryClient

@ContributesBinding(DataScope::class)
@Inject
class DefaultUpdateTaskMutationKey(
    private val apiClient: TasksApiClient,
    private val cacheDataStore: TasksCacheDataStore,
) : UpdateTaskMutationKey by buildMutationKey(
        id = MutationId("update_task"),
        mutate = { params ->
            // PATCH matches on task_id; the Edge Function returns {success:true} only,
            // so invalidate the cache and let the query refetch the canonical list.
            apiClient.updateTask(params.taskId, params.request)
            cacheDataStore.clear()
        },
    ) {
    // clear() above only wipes the DataStore preload; invalidate tasks_query so active
    // rememberQuery subscribers refetch the canonical list (subtask toggle, edits, etc.).
    override fun onMutateEffect(
        variable: UpdateTaskParams,
        data: Unit,
    ): Effect =
        {
            queryClient.invalidateQueriesBy(TasksQueryId)
        }
}
