package io.github.l2hyunwoo.kudos.navigation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.geometry.Offset
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import io.github.l2hyunwoo.core.design.transition.LocalNavAnimatedContentScope
import io.github.l2hyunwoo.kudos.AppGraph
import io.github.l2hyunwoo.kudos.core.common.navigation.Main
import io.github.l2hyunwoo.kudos.core.common.navigation.ProjectDetail
import io.github.l2hyunwoo.kudos.core.common.navigation.TaskDetail
import io.github.l2hyunwoo.main.MainScreen

context(appGraph: AppGraph)
fun NavGraphBuilder.mainScreenGraph(
    navController: NavHostController,
    darkTheme: Boolean,
    onToggleTheme: (Offset) -> Unit,
) {
    // `this` (the composable lambda receiver) is the AnimatedContentScope for the Main destination.
    composable<Main> {
        CompositionLocalProvider(LocalNavAnimatedContentScope provides this) {
            MainScreen(
                tasksContextFactory = appGraph,
                categoryContextFactory = appGraph,
                darkTheme = darkTheme,
                onToggleTheme = onToggleTheme,
                onNavigateToTaskDetail = { task ->
                    navController.navigate(
                        TaskDetail(
                            id = task.id,
                            taskId = task.taskId,
                            title = task.title,
                            description = task.description,
                            status = task.status.name,
                            priority = task.priority.name,
                            dueDate = task.dueDate,
                        )
                    )
                },
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
