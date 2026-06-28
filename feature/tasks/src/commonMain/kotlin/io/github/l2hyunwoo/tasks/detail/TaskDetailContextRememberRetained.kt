package io.github.l2hyunwoo.tasks.detail

import androidx.compose.runtime.Composable
import io.github.takahirom.rin.rememberRetained

@Composable
context(factory: TaskDetailContext.Factory)
fun rememberTaskDetailContextRetained() =
    rememberRetained {
        factory.createTaskDetailContext()
    }
