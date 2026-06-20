package io.github.l2hyunwoo.kudos.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.l2hyunwoo.category.CategoryListEntryPoint
import io.github.l2hyunwoo.category.rememberCategoryContextRetained
import io.github.l2hyunwoo.kudos.AppGraph
import io.github.l2hyunwoo.kudos.core.common.navigation.CategoryList
import io.github.l2hyunwoo.kudos.core.common.navigation.ProjectDetail

context(appGraph: AppGraph)
fun NavGraphBuilder.categoryListGraph(navController: NavHostController) {
    composable<CategoryList> {
        with(rememberCategoryContextRetained()) {
            CategoryListEntryPoint(
                onNavigateToProjectDetail = { projectId, categoryId, title, description, categoryColor, categoryPrefix ->
                    navController.navigate(
                        ProjectDetail(
                            projectId = projectId,
                            categoryId = categoryId,
                            title = title,
                            description = description,
                            categoryColor = categoryColor,
                            categoryPrefix = categoryPrefix
                        )
                    )
                }
            )
        }
    }
}
