package io.github.l2hyunwoo.data.categories.mutation

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.l2hyunwoo.data.categories.api.CategoriesApiClient
import io.github.l2hyunwoo.data.categories.cache.CategoriesCacheDataStore
import io.github.l2hyunwoo.data.categories.model.UpdateProjectMutationKey
import io.github.l2hyunwoo.kudos.core.common.DataScope
import soil.query.MutationId
import soil.query.buildMutationKey

@ContributesBinding(DataScope::class)
@Inject
class DefaultUpdateProjectMutationKey(
    private val apiClient: CategoriesApiClient,
    private val cacheDataStore: CategoriesCacheDataStore,
) : UpdateProjectMutationKey by buildMutationKey(
    id = MutationId("update_project"),
    mutate = { params ->
        val previousCache = cacheDataStore.getCacheSync()

        // Optimistic update: update project in the category
        val optimisticList = previousCache?.map { category ->
            if (category.id == params.categoryId) {
                category.copy(
                    projects = category.projects.map { project ->
                        if (project.id == params.projectId) {
                            project.copy(
                                title = params.request.title,
                                description = params.request.description
                            )
                        } else project
                    }
                )
            } else category
        } ?: emptyList()
        cacheDataStore.save(optimisticList)

        try {
            // If IDs have a temporary prefix, skip API call
            if (params.categoryId.startsWith("temp-") || params.projectId.startsWith("temp-")) {
                return@buildMutationKey optimisticList
            }

            // API call → returns updated list
            val updatedCategories = apiClient.updateProject(
                params.categoryId,
                params.projectId,
                params.request
            )
            cacheDataStore.save(updatedCategories)
            updatedCategories
        } catch (e: Exception) {
            previousCache?.let { cacheDataStore.save(it) }
            throw e
        }
    }
)
