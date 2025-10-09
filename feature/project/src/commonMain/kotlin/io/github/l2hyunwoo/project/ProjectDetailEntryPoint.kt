package io.github.l2hyunwoo.project

import androidx.compose.runtime.Composable
import io.github.l2hyunwoo.kudos.core.common.compose.rememberEventFlow

@Composable
context(context: ProjectContext)
fun ProjectDetailEntryPoint(
    projectId: String,
    categoryId: String,
    categoryPrefix: String,
    categoryColor: String,
    initialTitle: String,
    initialDescription: String?,
    onNavigateBack: () -> Unit
) {
    val eventFlow = rememberEventFlow<ProjectDetailEvent>()

    val uiState = projectDetailPresenter(
        projectId = projectId,
        categoryId = categoryId,
        categoryPrefix = categoryPrefix,
        categoryColor = categoryColor,
        initialTitle = initialTitle,
        initialDescription = initialDescription,
        eventFlow = eventFlow,
        onNavigateBack = onNavigateBack
    )

    ProjectDetailScreen(
        uiState = uiState,
        eventFlow = eventFlow
    )
}
