package io.github.l2hyunwoo.tasks.detail

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import io.github.l2hyunwoo.data.tasks.model.DeleteTaskMutationKey
import io.github.l2hyunwoo.data.tasks.model.TasksResponse
import io.github.l2hyunwoo.data.tasks.model.UpdateTaskMutationKey
import io.github.l2hyunwoo.kudos.core.common.ScreenContext
import soil.query.QueryKey

@GraphExtension(TaskDetailScope::class)
interface TaskDetailContext : ScreenContext {
    val updateTaskMutation: UpdateTaskMutationKey
    val deleteTaskMutation: DeleteTaskMutationKey

    // Same cached list the task-list screen reads; subtasks are filtered from it by parent_task_id.
    val tasksQuery: QueryKey<List<TasksResponse.CategoryWithTasks>>

    @ContributesTo(AppScope::class)
    @GraphExtension.Factory
    fun interface Factory {
        fun createTaskDetailContext(): TaskDetailContext
    }
}
