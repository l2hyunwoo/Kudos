package io.github.l2hyunwoo.kudos.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.github.l2hyunwoo.category.CategoryListEntryPoint
import io.github.l2hyunwoo.category.rememberCategoryContextRetained
import io.github.l2hyunwoo.kudos.AppGraph
import io.github.l2hyunwoo.kudos.core.common.navigation.CategoryList

context(appGraph: AppGraph)
fun NavGraphBuilder.categoryListGraph() {
    composable<CategoryList> {
        with(rememberCategoryContextRetained()) {
            CategoryListEntryPoint()
        }
    }
}
