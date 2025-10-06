package io.github.l2hyunwoo.data.categories.mutation

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.l2hyunwoo.data.categories.api.CategoriesApiClient
import io.github.l2hyunwoo.data.categories.cache.CategoriesCacheDataStore
import io.github.l2hyunwoo.data.categories.model.CreateProjectMutationKey
import io.github.l2hyunwoo.data.categories.model.Project
import io.github.l2hyunwoo.kudos.core.common.DataScope
import kotlin.time.Clock
import soil.query.MutationId
import soil.query.buildMutationKey
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@ContributesBinding(DataScope::class)
@Inject
class DefaultCreateProjectMutationKey(
    private val apiClient: CategoriesApiClient,
    private val cacheDataStore: CategoriesCacheDataStore,
) : CreateProjectMutationKey by buildMutationKey(
    id = MutationId("create_project"),
    mutate = { params ->
        val previousCache = cacheDataStore.getCacheSync()

        // Optimistic update: add project to the category
        val optimisticProject = Project(
            id = "temp-${Clock.System.now().toEpochMilliseconds()}",
            title = params.request.title,
            description = params.request.description,
            createdAt = Clock.System.now().toString(),
            updatedAt = Clock.System.now().toString()
        )
        val optimisticList = previousCache?.map { category ->
            if (category.id == params.categoryId) {
                category.copy(projects = category.projects + optimisticProject)
            } else category
        } ?: emptyList()
        cacheDataStore.save(optimisticList)

        try {
            // If category has a temporary ID, skip API call
            if (params.categoryId.startsWith("temp-")) {
                return@buildMutationKey optimisticList
            }

            // API call → returns updated list
            val updatedCategories = apiClient.createProject(params.categoryId, params.request)
            cacheDataStore.save(updatedCategories)
            updatedCategories
        } catch (e: Exception) {
            previousCache?.let { cacheDataStore.save(it) }
            throw e
        }
    }
)
