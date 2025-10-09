package io.github.l2hyunwoo.data.categories.api

import io.github.l2hyunwoo.data.categories.model.CategoriesResponse
import io.github.l2hyunwoo.data.categories.model.CreateCategoryRequest
import io.github.l2hyunwoo.data.categories.model.CreateProjectRequest
import io.github.l2hyunwoo.data.categories.model.UpdateProjectRequest

interface CategoriesApiClient {
    suspend fun getCategories(): CategoriesResponse

    suspend fun createCategory(request: CreateCategoryRequest): CategoriesResponse

    suspend fun deleteCategory(id: String): CategoriesResponse

    suspend fun createProject(categoryId: String, request: CreateProjectRequest): CategoriesResponse

    suspend fun updateProject(categoryId: String, projectId: String, request: UpdateProjectRequest): CategoriesResponse

    suspend fun deleteProject(categoryId: String, projectId: String): CategoriesResponse
}
