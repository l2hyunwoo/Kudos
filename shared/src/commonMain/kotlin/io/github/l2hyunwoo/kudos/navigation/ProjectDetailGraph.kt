package io.github.l2hyunwoo.kudos.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import io.github.l2hyunwoo.kudos.AppGraph
import io.github.l2hyunwoo.kudos.core.common.navigation.ProjectDetail
import io.github.l2hyunwoo.kudos.core.common.navigation.TaskDetail
import io.github.l2hyunwoo.project.ProjectDetailEntryPoint
import io.github.l2hyunwoo.project.rememberProjectContextRetained

context(appGraph: AppGraph)
fun NavGraphBuilder.projectDetailGraph(navController: NavHostController) {
    composable<ProjectDetail> { backStackEntry ->
        val args = backStackEntry.toRoute<ProjectDetail>()
        with(rememberProjectContextRetained()) {
            ProjectDetailEntryPoint(
                projectId = args.projectId,
                categoryId = args.categoryId,
                categoryPrefix = args.categoryPrefix,
                categoryColor = args.categoryColor,
                initialTitle = args.title,
                initialDescription = args.description,
                onNavigateBack = { navController.navigateUp() },
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
                        ),
                    )
                },
            )
        }
    }
}
