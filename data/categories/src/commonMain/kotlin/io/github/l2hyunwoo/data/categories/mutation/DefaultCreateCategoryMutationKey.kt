package io.github.l2hyunwoo.data.categories.mutation

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.l2hyunwoo.data.categories.api.CategoriesApiClient
import io.github.l2hyunwoo.data.categories.cache.CategoriesCacheDataStore
import io.github.l2hyunwoo.data.categories.model.Category
import io.github.l2hyunwoo.data.categories.model.CreateCategoryMutationKey
import io.github.l2hyunwoo.kudos.core.common.DataScope
import kotlin.time.Clock
import soil.query.MutationId
import soil.query.buildMutationKey
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@ContributesBinding(DataScope::class)
@Inject
class DefaultCreateCategoryMutationKey(
    private val apiClient: CategoriesApiClient,
    private val cacheDataStore: CategoriesCacheDataStore,
) : CreateCategoryMutationKey by buildMutationKey(
    id = MutationId("create_category"),
    mutate = { request ->
        // 1. Backup current cache
        val previousCache = cacheDataStore.getCacheSync()

        // 2. Optimistic update: add immediately with temp ID
        val optimisticCategory = Category(
            id = "temp-${Clock.System.now().toEpochMilliseconds()}",
            prefix = request.prefix,
            title = request.title,
            color = request.color ?: "#FF6B6B",
            createdAt = Clock.System.now().toString(),
            updatedAt = Clock.System.now().toString(),
            projects = emptyList()
        )
        val optimisticList = (previousCache ?: emptyList()) + optimisticCategory
        cacheDataStore.save(optimisticList)

        try {
            // 3. API call → returns updated list
            val updatedCategories = apiClient.createCategory(request)

            // 4. Update cache with server response
            cacheDataStore.save(updatedCategories)

            // 5. Return as mutation result
            updatedCategories
        } catch (e: Exception) {
            // 6. Rollback on failure
            previousCache?.let { cacheDataStore.save(it) }
            throw e
        }
    }
)
