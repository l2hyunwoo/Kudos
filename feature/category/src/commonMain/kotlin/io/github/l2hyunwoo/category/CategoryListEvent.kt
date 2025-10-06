package io.github.l2hyunwoo.category

import io.github.l2hyunwoo.data.categories.model.CreateCategoryRequest
import io.github.l2hyunwoo.data.categories.model.CreateProjectRequest
import io.github.l2hyunwoo.data.categories.model.Project

sealed interface CategoryListEvent {
    data class CreateCategory(val request: CreateCategoryRequest) : CategoryListEvent
    data class DeleteCategory(val categoryId: String) : CategoryListEvent
    data class CreateProject(
        val categoryId: String,
        val request: CreateProjectRequest
    ) : CategoryListEvent

    data class DeleteProject(
        val categoryId: String,
        val projectId: String
    ) : CategoryListEvent

    data class UndoDeleteProject(
        val categoryId: String,
        val project: Project
    ) : CategoryListEvent

    data class ConfirmDeleteProject(
        val categoryId: String,
        val projectId: String
    ) : CategoryListEvent
}
