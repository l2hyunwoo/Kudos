package io.github.l2hyunwoo.data.categories.mutation

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.l2hyunwoo.data.categories.api.CategoriesApiClient
import io.github.l2hyunwoo.data.categories.cache.CategoriesCacheDataStore
import io.github.l2hyunwoo.data.categories.model.DeleteCategoryMutationKey
import io.github.l2hyunwoo.kudos.core.common.DataScope
import soil.query.MutationId
import soil.query.buildMutationKey

@ContributesBinding(DataScope::class)
@Inject
class DefaultDeleteCategoryMutationKey(
    private val apiClient: CategoriesApiClient,
    private val cacheDataStore: CategoriesCacheDataStore,
) : DeleteCategoryMutationKey by buildMutationKey(
    id = MutationId("delete_category"),
    mutate = { categoryId ->
        val previousCache = cacheDataStore.getCacheSync()

        // Optimistic update: remove immediately
        val optimisticList = previousCache?.filterNot { it.id == categoryId } ?: emptyList()
        cacheDataStore.save(optimisticList)

        try {
            // If it's a temporary ID (from optimistic create), skip API call
            if (categoryId.startsWith("temp-")) {
                return@buildMutationKey optimisticList
            }

            // API call → returns updated list
            val updatedCategories = apiClient.deleteCategory(categoryId)
            cacheDataStore.save(updatedCategories)
            updatedCategories
        } catch (e: Exception) {
            previousCache?.let { cacheDataStore.save(it) }
            throw e
        }
    }
)
