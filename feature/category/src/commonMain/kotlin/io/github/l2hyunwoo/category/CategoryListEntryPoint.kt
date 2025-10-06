package io.github.l2hyunwoo.category

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import io.github.l2hyunwoo.kudos.core.soil.SoilBoundary
import io.github.l2hyunwoo.kudos.core.soil.SoilFallbackDefaults
import kotlinx.collections.immutable.toImmutableList
import soil.query.compose.rememberQuery

@OptIn(ExperimentalMaterial3Api::class)
@Composable
context(context: CategoryContext)
fun CategoryListEntryPoint() {
    SoilBoundary(
        state = rememberQuery(context.categoriesQuery),
        fallback = SoilFallbackDefaults.appBar(
            title = "Categories",
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { /* TODO: Add category */ },
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "Add Category",
                    )
                }
            }
        ),
    ) { categories ->
        CategoryListScreen(
            categories = categories.toImmutableList()
        )
    }
}
