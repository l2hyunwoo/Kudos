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
    categories: List<TasksResponse.CategoryWithTasks>,
    searchQuery: String = "",
): TaskListUiState {
    val createTaskMutation = rememberMutation(context.createTaskMutation)

    EventEffect(eventFlow) { event ->
        when (event) {
            is TaskListEvent.CreateTask -> {
                createTaskMutation.mutate(event.request)
            }
        }
    }

    val query = searchQuery.trim()
    val visibleCategories = if (query.isEmpty()) {
        categories
    } else {
        // Substring match on title/description; categories with no match drop out so the screen's
        // empty state ("결과 없음") shows when nothing matches.
        categories.mapNotNull { category ->
            val matches = category.tasks.filter { task ->
                task.title.contains(query, ignoreCase = true) ||
                    task.description?.contains(query, ignoreCase = true) == true
            }
            if (matches.isEmpty()) null else category.copy(tasks = matches)
        }
    }

    return TaskListUiState(
        categories = visibleCategories.toImmutableList(),
        isLoading = createTaskMutation.isPending,
        error = createTaskMutation.error,
        searchQuery = query,
    )
}
