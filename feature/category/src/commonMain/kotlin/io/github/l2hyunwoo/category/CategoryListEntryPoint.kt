package io.github.l2hyunwoo.category

import androidx.compose.runtime.Composable
import io.github.l2hyunwoo.kudos.core.common.compose.rememberEventFlow
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.compose.rememberQuery
import soil.query.compose.rememberSubscription

@OptIn(ExperimentalSoilQueryApi::class)
@Composable
context(context: CategoryContext)
fun CategoryListEntryPoint(
    onNavigateToProjectDetail: (String, String, String, String?, String, String) -> Unit = { _, _, _, _, _, _ -> }
) {
    val eventFlow = rememberEventFlow<CategoryListEvent>()

    val categoriesQuery = rememberQuery(context.categoriesQuery)
    val categoriesSubscription = rememberSubscription(context.categoriesSubscription)

    val categories = categoriesSubscription.data ?: categoriesQuery.data ?: emptyList()

    val uiState = categoryListPresenter(
        eventFlow = eventFlow,
        categories = categories
    )

    CategoryListScreen(
        uiState = uiState,
        eventFlow = eventFlow,
        onNavigateToProjectDetail = onNavigateToProjectDetail
    )
}
