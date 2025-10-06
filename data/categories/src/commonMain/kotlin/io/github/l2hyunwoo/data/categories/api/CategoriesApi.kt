package io.github.l2hyunwoo.data.categories.api

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import io.github.l2hyunwoo.data.categories.model.CategoriesResponse
import io.github.l2hyunwoo.data.categories.model.CreateCategoryRequest
import io.github.l2hyunwoo.data.categories.model.CreateProjectRequest

interface CategoriesApi {
    @GET("functions/v1/categories-api/categories")
    suspend fun getCategories(): CategoriesResponse

    @POST("functions/v1/categories-api/categories")
    suspend fun createCategory(@Body request: CreateCategoryRequest): CategoriesResponse

    @DELETE("functions/v1/categories-api/categories/{id}")
    suspend fun deleteCategory(@Path("id") id: String): CategoriesResponse

    @POST("functions/v1/categories-api/categories/{id}/projects")
    suspend fun createProject(
        @Path("id") categoryId: String,
        @Body request: CreateProjectRequest
    ): CategoriesResponse

    @DELETE("functions/v1/categories-api/categories/{categoryId}/projects/{projectId}")
    suspend fun deleteProject(
        @Path("categoryId") categoryId: String,
        @Path("projectId") projectId: String
    ): CategoriesResponse
}
