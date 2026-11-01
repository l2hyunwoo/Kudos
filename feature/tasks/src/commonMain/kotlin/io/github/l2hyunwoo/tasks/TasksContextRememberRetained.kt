package io.github.l2hyunwoo.tasks

import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain

@Composable
context(factory: TasksContext.Factory)
fun rememberTasksContextRetained() =
    retain {
        factory.createTasksContext()
    }
