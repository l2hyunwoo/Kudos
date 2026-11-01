package io.github.l2hyunwoo.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain

@Composable
context(factory: ProjectContext.Factory)
fun rememberProjectContextRetained() =
    retain {
        factory.createProjectContext()
    }
