package io.github.l2hyunwoo.data.categories.subscription

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.github.l2hyunwoo.data.categories.cache.CategoriesCacheDataStore
import io.github.l2hyunwoo.data.categories.model.CategoriesSubscriptionKey
import io.github.l2hyunwoo.kudos.core.common.DataScope
import soil.query.SubscriptionId
import soil.query.buildSubscriptionKey

@ContributesBinding(DataScope::class)
@Inject
class DefaultCategoriesSubscriptionKey(
    private val cacheDataStore: CategoriesCacheDataStore,
) : CategoriesSubscriptionKey by buildSubscriptionKey(
    id = SubscriptionId("categories"),
    subscribe = { cacheDataStore.getCacheStream() }
)
