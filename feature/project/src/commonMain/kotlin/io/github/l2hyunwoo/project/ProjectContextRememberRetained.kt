package io.github.l2hyunwoo.project

import androidx.compose.runtime.Composable
import io.github.takahirom.rin.rememberRetained

@Composable
context(factory: ProjectContext.Factory)
fun rememberProjectContextRetained() = rememberRetained {
    factory.createProjectContext()
}
