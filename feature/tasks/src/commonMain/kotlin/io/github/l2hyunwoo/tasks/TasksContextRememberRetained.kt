package io.github.l2hyunwoo.tasks

import androidx.compose.runtime.Composable
import io.github.takahirom.rin.rememberRetained

@Composable
context(factory: TasksContext.Factory)
fun rememberTasksContextRetained() = rememberRetained {
    factory.createTasksContext()
}
