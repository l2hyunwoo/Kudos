package io.github.l2hyunwoo.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.l2hyunwoo.data.categories.model.Category
import io.github.l2hyunwoo.data.categories.model.CreateProjectParams
import io.github.l2hyunwoo.data.categories.model.DeleteProjectParams
import io.github.l2hyunwoo.kudos.core.common.compose.EventEffect
import io.github.l2hyunwoo.kudos.core.common.compose.EventFlow
import kotlinx.collections.immutable.toImmutableList
import soil.query.compose.rememberMutation

@Composable
context(context: CategoryContext)
fun categoryListPresenter(
    eventFlow: EventFlow<CategoryListEvent>,
    categories: List<Category>
): CategoryListUiState {
    val createCategoryMutation = rememberMutation(context.createCategoryMutation)
    val deleteCategoryMutation = rememberMutation(context.deleteCategoryMutation)
    val createProjectMutation = rememberMutation(context.createProjectMutation)
    val deleteProjectMutation = rememberMutation(context.deleteProjectMutation)

    var deletedProject by remember { mutableStateOf<DeletedProjectInfo?>(null) }

    EventEffect(eventFlow) { event ->
        when (event) {
            is CategoryListEvent.CreateCategory -> {
                createCategoryMutation.mutate(event.request)
            }
            is CategoryListEvent.DeleteCategory -> {
                deleteCategoryMutation.mutate(event.categoryId)
            }
            is CategoryListEvent.CreateProject -> {
                createProjectMutation.mutate(
                    CreateProjectParams(event.categoryId, event.request)
                )
            }
            is CategoryListEvent.DeleteProject -> {
                // Use the project directly from the event (no need to search)
                deletedProject = DeletedProjectInfo(event.categoryId, event.project)
            }
            is CategoryListEvent.UndoDeleteProject -> {
                // Clear the deletedProject state (cancel deletion)
                deletedProject = null
            }
            is CategoryListEvent.ConfirmDeleteProject -> {
                // Actually delete via API
                deleteProjectMutation.mutate(
                    DeleteProjectParams(event.categoryId, event.projectId)
                )
                deletedProject = null
            }
        }
    }

    return CategoryListUiState(
        categories = categories.toImmutableList(),
        isLoading = createCategoryMutation.isPending
            || deleteCategoryMutation.isPending
            || createProjectMutation.isPending
            || deleteProjectMutation.isPending,
        error = createCategoryMutation.error
            ?: deleteCategoryMutation.error
            ?: createProjectMutation.error
            ?: deleteProjectMutation.error,
        deletedProject = deletedProject
    )
}
