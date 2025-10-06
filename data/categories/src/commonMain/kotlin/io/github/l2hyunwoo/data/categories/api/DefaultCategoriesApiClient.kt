package io.github.l2hyunwoo.data.categories.api

import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.l2hyunwoo.data.categories.model.CategoriesResponse
import io.github.l2hyunwoo.data.categories.model.CreateCategoryRequest
import io.github.l2hyunwoo.data.categories.model.CreateProjectRequest
import io.github.l2hyunwoo.kudos.core.common.DataScope

@ContributesBinding(DataScope::class)
@Inject
class DefaultCategoriesApiClient internal constructor(
    ktorfit: Ktorfit
) : CategoriesApiClient {
    private val categoriesApi = ktorfit.createCategoriesApi()

    override suspend fun getCategories(): CategoriesResponse {
        return categoriesApi.getCategories()
    }

    override suspend fun createCategory(request: CreateCategoryRequest): CategoriesResponse {
        return categoriesApi.createCategory(request)
    }

    override suspend fun deleteCategory(id: String): CategoriesResponse {
        return categoriesApi.deleteCategory(id)
    }

    override suspend fun createProject(categoryId: String, request: CreateProjectRequest): CategoriesResponse {
        return categoriesApi.createProject(categoryId, request)
    }

    override suspend fun deleteProject(categoryId: String, projectId: String): CategoriesResponse {
        return categoriesApi.deleteProject(categoryId, projectId)
    }
}
