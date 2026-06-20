package io.github.l2hyunwoo.kudos.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.l2hyunwoo.kudos.AppGraph
import io.github.l2hyunwoo.kudos.core.common.navigation.Main
import io.github.l2hyunwoo.kudos.core.common.navigation.ProjectDetail
import io.github.l2hyunwoo.main.MainScreen

context(appGraph: AppGraph)
fun NavGraphBuilder.mainScreenGraph(navController: NavHostController) {
    composable<Main> {
        MainScreen(
            tasksContextFactory = appGraph,
            categoryContextFactory = appGraph,
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
