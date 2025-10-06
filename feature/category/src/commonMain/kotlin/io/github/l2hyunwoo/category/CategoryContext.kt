package io.github.l2hyunwoo.category

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import io.github.l2hyunwoo.data.categories.model.Category
import io.github.l2hyunwoo.kudos.core.common.ScreenContext
import soil.query.QueryKey

@GraphExtension(CategoryScope::class)
interface CategoryContext: ScreenContext {
    val categoriesQuery: QueryKey<List<Category>>

    @ContributesTo(AppScope::class)
    @GraphExtension.Factory
    fun interface Factory {
        fun createCategoryContext(): CategoryContext
    }
}
