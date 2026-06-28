package io.github.l2hyunwoo.category

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.kudos.core.common.compose.EventFlow
import io.github.l2hyunwoo.kudos.core.common.compose.rememberEventFlow
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.compose.rememberQuery
import soil.query.compose.rememberSubscription

@OptIn(ExperimentalSoilQueryApi::class)
@Composable
context(context: CategoryContext)
fun CategoryListEntryPoint(
    eventFlow: EventFlow<CategoryListEvent>? = null,
    searchQuery: String = "",
    // Top contentPadding so the list clears the glass header owned by the parent (MainScreen) and
    // the rest scrolls under it. Standalone usage passes 0.
    topContentPadding: Dp = 0.dp,
    onNavigateToProjectDetail: (String, String, String, String?, String, String) -> Unit = { _, _, _, _, _, _ -> },
) {
    val actualEventFlow = eventFlow ?: rememberEventFlow()

    val categoriesQuery = rememberQuery(context.categoriesQuery)
    val categoriesSubscription = rememberSubscription(context.categoriesSubscription)

    val categories = categoriesSubscription.data ?: categoriesQuery.data ?: emptyList()

    val uiState =
        categoryListPresenter(
            eventFlow = actualEventFlow,
            categories = categories,
            searchQuery = searchQuery,
        )

    CategoryListScreen(
        uiState = uiState,
        eventFlow = actualEventFlow,
        topContentPadding = topContentPadding,
        onNavigateToProjectDetail = onNavigateToProjectDetail,
    )
}
