package io.github.l2hyunwoo.tasks

import androidx.compose.runtime.Composable
import io.github.l2hyunwoo.data.tasks.model.TasksResponse
import io.github.l2hyunwoo.kudos.core.common.compose.EventEffect
import io.github.l2hyunwoo.kudos.core.common.compose.EventFlow
import kotlinx.collections.immutable.toImmutableList
import soil.query.compose.rememberMutation

@Composable
context(context: TasksContext)
fun taskListPresenter(
    eventFlow: EventFlow<TaskListEvent>,
    categories: List<TasksResponse.CategoryWithTasks>
): TaskListUiState {
    val createTaskMutation = rememberMutation(context.createTaskMutation)

    EventEffect(eventFlow) { event ->
        when (event) {
            is TaskListEvent.CreateTask -> {
                createTaskMutation.mutate(event.request)
            }
        }
    }

    return TaskListUiState(
        categories = categories.toImmutableList(),
        isLoading = createTaskMutation.isPending,
        error = createTaskMutation.error
    )
}
