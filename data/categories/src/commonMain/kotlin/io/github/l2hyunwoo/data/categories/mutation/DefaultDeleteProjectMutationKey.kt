package io.github.l2hyunwoo.data.categories.mutation

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.l2hyunwoo.data.categories.api.CategoriesApiClient
import io.github.l2hyunwoo.data.categories.cache.CategoriesCacheDataStore
import io.github.l2hyunwoo.data.categories.model.DeleteProjectMutationKey
import io.github.l2hyunwoo.kudos.core.common.DataScope
import soil.query.MutationId
import soil.query.buildMutationKey

@ContributesBinding(DataScope::class)
@Inject
class DefaultDeleteProjectMutationKey(
    private val apiClient: CategoriesApiClient,
    private val cacheDataStore: CategoriesCacheDataStore,
) : DeleteProjectMutationKey by buildMutationKey(
    id = MutationId("delete_project"),
    mutate = { params ->
        val previousCache = cacheDataStore.getCacheSync()

        // Optimistic update: remove project from the category
        val optimisticList = previousCache?.map { category ->
            if (category.id == params.categoryId) {
                category.copy(projects = category.projects.filterNot { it.id == params.projectId })
            } else category
        } ?: emptyList()
        cacheDataStore.save(optimisticList)

        try {
            // If project has a temporary ID, skip API call
            if (params.projectId.startsWith("temp-")) {
                return@buildMutationKey optimisticList
            }

            // API call → returns updated list
            val updatedCategories = apiClient.deleteProject(params.categoryId, params.projectId)
            cacheDataStore.save(updatedCategories)
            updatedCategories
        } catch (e: Exception) {
            previousCache?.let { cacheDataStore.save(it) }
            throw e
        }
    }
)
