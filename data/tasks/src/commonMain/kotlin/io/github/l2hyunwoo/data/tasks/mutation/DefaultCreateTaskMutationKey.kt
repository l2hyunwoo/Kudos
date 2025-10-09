package io.github.l2hyunwoo.data.tasks.mutation

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.l2hyunwoo.data.tasks.api.TasksApiClient
import io.github.l2hyunwoo.data.tasks.cache.TasksCacheDataStore
import io.github.l2hyunwoo.data.tasks.model.CreateTaskMutationKey
import io.github.l2hyunwoo.kudos.core.common.DataScope
import soil.query.MutationId
import soil.query.buildMutationKey

@ContributesBinding(DataScope::class)
@Inject
class DefaultCreateTaskMutationKey(
    private val apiClient: TasksApiClient,
    private val cacheDataStore: TasksCacheDataStore,
) : CreateTaskMutationKey by buildMutationKey(
    id = MutationId("create_task"),
    mutate = { request ->
        // 1. Create task via API
        apiClient.createTask(request)

        // 2. Invalidate cache to trigger refresh
        cacheDataStore.clear()
    }
)
