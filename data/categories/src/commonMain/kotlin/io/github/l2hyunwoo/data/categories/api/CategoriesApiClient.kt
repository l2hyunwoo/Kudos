package io.github.l2hyunwoo.data.categories.api

import io.github.l2hyunwoo.data.categories.model.CategoriesResponse
import io.github.l2hyunwoo.data.categories.model.CreateCategoryRequest
import io.github.l2hyunwoo.data.categories.model.CreateProjectRequest

interface CategoriesApiClient {
    suspend fun getCategories(): CategoriesResponse

    suspend fun createCategory(request: CreateCategoryRequest): CategoriesResponse

    suspend fun deleteCategory(id: String): CategoriesResponse

    suspend fun createProject(categoryId: String, request: CreateProjectRequest): CategoriesResponse

    suspend fun deleteProject(categoryId: String, projectId: String): CategoriesResponse
}
