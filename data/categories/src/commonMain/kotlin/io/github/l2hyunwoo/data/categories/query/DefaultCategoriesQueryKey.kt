package io.github.l2hyunwoo.data.categories.query

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.l2hyunwoo.data.categories.api.CategoriesApiClient
import io.github.l2hyunwoo.data.categories.cache.CategoriesCacheDataStore
import io.github.l2hyunwoo.data.categories.model.Category
import io.github.l2hyunwoo.kudos.core.common.DataScope
import soil.query.QueryId
import soil.query.QueryKey
import soil.query.QueryPreloadData
import soil.query.buildQueryKey

@ContributesBinding(DataScope::class)
@Inject
class DefaultCategoriesQueryKey(
    private val apiClient: CategoriesApiClient,
    private val dataStore: CategoriesCacheDataStore,
) : QueryKey<List<Category>> by buildQueryKey(
    id = QueryId("categories_query"),
    fetch = {
        val response = apiClient.getCategories()
        dataStore.save(response)
        response
    }
) {
    override fun onPreloadData(): QueryPreloadData<List<Category>>? {
        return { dataStore.getCache() }
    }
}
