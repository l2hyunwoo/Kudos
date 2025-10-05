package io.github.l2hyunwoo.data.tasks

import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.l2hyunwoo.data.tasks.model.TasksResponse
import io.github.l2hyunwoo.kudos.core.network.di.NetworkScope

@ContributesBinding(NetworkScope::class)
@Inject
class DefaultTasksApiClient internal constructor(ktorfit: Ktorfit) : TasksApiClient {
    override suspend fun getTasks(): TasksResponse {
        TODO("Not yet implemented")
    }
}