package io.github.l2hyunwoo.kudos.navigation

import androidx.compose.runtime.CompositionLocalProvider
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import io.github.l2hyunwoo.core.design.transition.LocalNavAnimatedContentScope
import io.github.l2hyunwoo.data.tasks.model.TaskPriority
import io.github.l2hyunwoo.data.tasks.model.TaskStatus
import io.github.l2hyunwoo.kudos.AppGraph
import io.github.l2hyunwoo.kudos.core.common.navigation.TaskDetail
import io.github.l2hyunwoo.tasks.detail.TaskDetailEntryPoint
import io.github.l2hyunwoo.tasks.detail.rememberTaskDetailContextRetained

context(appGraph: AppGraph)
fun NavGraphBuilder.taskDetailGraph(navController: NavHostController) {
    // `this` (the composable lambda receiver) is the AnimatedContentScope for the TaskDetail route.
    composable<TaskDetail> { backStackEntry ->
        val args = backStackEntry.toRoute<TaskDetail>()
        CompositionLocalProvider(LocalNavAnimatedContentScope provides this) {
            with(rememberTaskDetailContextRetained()) {
                TaskDetailEntryPoint(
                    id = args.id,
                    taskId = args.taskId,
                    title = args.title,
                    description = args.description,
                    status = TaskStatus.valueOf(args.status),
                    priority = TaskPriority.valueOf(args.priority),
                    dueDate = args.dueDate,
                    onNavigateBack = { navController.navigateUp() }
                )
            }
        }
    }
}
