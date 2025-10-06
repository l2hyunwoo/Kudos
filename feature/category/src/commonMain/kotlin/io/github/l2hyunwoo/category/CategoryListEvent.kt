package io.github.l2hyunwoo.category

import io.github.l2hyunwoo.data.categories.model.CreateCategoryRequest
import io.github.l2hyunwoo.data.categories.model.CreateProjectRequest

sealed interface CategoryListEvent {
    data class CreateCategory(val request: CreateCategoryRequest) : CategoryListEvent
    data class DeleteCategory(val categoryId: String) : CategoryListEvent
    data class CreateProject(
        val categoryId: String,
        val request: CreateProjectRequest
    ) : CategoryListEvent
}
