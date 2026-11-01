package io.github.l2hyunwoo.tasks.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain

@Composable
context(factory: TaskDetailContext.Factory)
fun rememberTaskDetailContextRetained() =
    retain {
        factory.createTaskDetailContext()
    }
