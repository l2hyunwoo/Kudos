package io.github.l2hyunwoo.kudos.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import io.github.l2hyunwoo.kudos.AppGraph
import io.github.l2hyunwoo.kudos.core.common.navigation.TaskList
import io.github.l2hyunwoo.tasks.TaskListEntryPoint
import io.github.l2hyunwoo.tasks.rememberTasksContextRetained

context(appGraph: AppGraph)
fun NavGraphBuilder.taskListGraph() {
    composable<TaskList> {
        with(rememberTasksContextRetained()) {
            TaskListEntryPoint()
        }
    }
}
