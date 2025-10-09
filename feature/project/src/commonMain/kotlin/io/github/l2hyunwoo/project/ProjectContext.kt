package io.github.l2hyunwoo.project

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import io.github.l2hyunwoo.data.categories.model.UpdateProjectMutationKey
import io.github.l2hyunwoo.data.tasks.query.DefaultProjectTasksQueryKeyFactory
import io.github.l2hyunwoo.kudos.core.common.ScreenContext

@GraphExtension(ProjectScope::class)
interface ProjectContext : ScreenContext {
    val projectTasksQueryKeyFactory: DefaultProjectTasksQueryKeyFactory
    val updateProjectMutation: UpdateProjectMutationKey

    @ContributesTo(AppScope::class)
    @GraphExtension.Factory
    fun interface Factory {
        fun createProjectContext(): ProjectContext
    }
}
