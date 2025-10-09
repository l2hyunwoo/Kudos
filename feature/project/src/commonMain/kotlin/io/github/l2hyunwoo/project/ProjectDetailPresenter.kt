package io.github.l2hyunwoo.project

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.l2hyunwoo.data.categories.model.UpdateProjectParams
import io.github.l2hyunwoo.kudos.core.common.compose.EventEffect
import io.github.l2hyunwoo.kudos.core.common.compose.EventFlow
import kotlinx.collections.immutable.toImmutableList
import soil.query.compose.rememberMutation
import soil.query.compose.rememberQuery

@Composable
context(context: ProjectContext)
fun projectDetailPresenter(
    projectId: String,
    categoryId: String,
    categoryPrefix: String,
    categoryColor: String,
    initialTitle: String,
    initialDescription: String?,
    eventFlow: EventFlow<ProjectDetailEvent>,
    onNavigateBack: () -> Unit
): ProjectDetailUiState {
    // Query for project tasks
    val tasksQueryKey = remember(projectId) { context.projectTasksQueryKeyFactory.create(projectId) }
    val tasksQuery = rememberQuery(tasksQueryKey)

    // Mutation for updating project
    val updateProjectMutation = rememberMutation(context.updateProjectMutation)

    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var showEditSheet by remember { mutableStateOf(false) }

    // Handle mutation success - update local state
    LaunchedEffect(updateProjectMutation.data) {
        updateProjectMutation.data?.let {
            // Find the updated project in the response
            it.flatMap { category -> category.projects }
                .find { project -> project.id == projectId }
                ?.let { updatedProject ->
                    title = updatedProject.title
                    description = updatedProject.description
                }
        }
    }

    EventEffect(eventFlow) { event ->
        when (event) {
            is ProjectDetailEvent.ShowEditSheet -> {
                showEditSheet = true
            }
            is ProjectDetailEvent.DismissEditSheet -> {
                showEditSheet = false
            }
            is ProjectDetailEvent.UpdateProject -> {
                updateProjectMutation.mutate(
                    UpdateProjectParams(
                        categoryId = categoryId,
                        projectId = projectId,
                        request = event.request
                    )
                )
                showEditSheet = false
            }
            is ProjectDetailEvent.NavigateBack -> {
                onNavigateBack()
            }
        }
    }

    return ProjectDetailUiState(
        projectId = projectId,
        categoryId = categoryId,
        categoryPrefix = categoryPrefix,
        categoryColor = categoryColor,
        title = title,
        description = description,
        tasks = (tasksQuery.data ?: emptyList()).toImmutableList(),
        isLoadingTasks = tasksQuery.isPending,
        isUpdatingProject = updateProjectMutation.isPending,
        showEditSheet = showEditSheet,
        error = tasksQuery.error ?: updateProjectMutation.error
    )
}
