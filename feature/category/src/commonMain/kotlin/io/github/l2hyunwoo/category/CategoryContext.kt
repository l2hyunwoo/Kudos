package io.github.l2hyunwoo.category

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.GraphExtension
import io.github.l2hyunwoo.data.categories.model.CategoriesSubscriptionKey
import io.github.l2hyunwoo.data.categories.model.Category
import io.github.l2hyunwoo.data.categories.model.CreateCategoryMutationKey
import io.github.l2hyunwoo.data.categories.model.CreateProjectMutationKey
import io.github.l2hyunwoo.data.categories.model.DeleteCategoryMutationKey
import io.github.l2hyunwoo.kudos.core.common.ScreenContext
import soil.query.QueryKey

@GraphExtension(CategoryScope::class)
interface CategoryContext: ScreenContext {
    val categoriesQuery: QueryKey<List<Category>>
    val categoriesSubscription: CategoriesSubscriptionKey

    val createCategoryMutation: CreateCategoryMutationKey
    val deleteCategoryMutation: DeleteCategoryMutationKey
    val createProjectMutation: CreateProjectMutationKey

    @ContributesTo(AppScope::class)
    @GraphExtension.Factory
    fun interface Factory {
        fun createCategoryContext(): CategoryContext
    }
}
