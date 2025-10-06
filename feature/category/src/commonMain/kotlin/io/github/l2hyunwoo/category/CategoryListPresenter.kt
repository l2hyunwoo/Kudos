package io.github.l2hyunwoo.category

import androidx.compose.runtime.Composable
import io.github.l2hyunwoo.data.categories.model.Category
import io.github.l2hyunwoo.data.categories.model.CreateProjectParams
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
        }
    }

    return CategoryListUiState(
        categories = categories.toImmutableList(),
        isLoading = createCategoryMutation.isPending
            || deleteCategoryMutation.isPending
            || createProjectMutation.isPending,
        error = createCategoryMutation.error
            ?: deleteCategoryMutation.error
            ?: createProjectMutation.error
    )
}
