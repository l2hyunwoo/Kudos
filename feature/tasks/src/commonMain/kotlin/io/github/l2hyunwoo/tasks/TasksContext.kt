package io.github.l2hyunwoo.tasks

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import io.github.l2hyunwoo.data.tasks.model.TasksResponse
import io.github.l2hyunwoo.kudos.core.common.ScreenContext
import soil.query.QueryKey

@GraphExtension(TasksScope::class)
interface TasksContext: ScreenContext {
    val tasksQuery: QueryKey<List<TasksResponse.CategoryWithTasks>>

    @ContributesTo(AppScope::class)
    @GraphExtension.Factory
    fun interface Factory {
        fun createTasksContext(): TasksContext
    }
}
