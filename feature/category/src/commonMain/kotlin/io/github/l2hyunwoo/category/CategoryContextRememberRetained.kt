package io.github.l2hyunwoo.category

import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.retain

@Composable
context(factory: CategoryContext.Factory)
fun rememberCategoryContextRetained() =
    retain {
        factory.createCategoryContext()
    }
