package io.github.l2hyunwoo.category

import androidx.compose.runtime.Composable
import io.github.takahirom.rin.rememberRetained

@Composable
context(factory: CategoryContext.Factory)
fun rememberCategoryContextRetained() = rememberRetained {
    factory.createCategoryContext()
}