package io.github.l2hyunwoo.project

import io.github.l2hyunwoo.data.tasks.model.Task
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class ProjectDetailUiState(
    val projectId: String,
    val categoryId: String,
    val categoryPrefix: String,
    val categoryColor: String,
    val title: String,
    val description: String?,
    val tasks: ImmutableList<Task> = persistentListOf(),
    val isLoadingTasks: Boolean = false,
    val isUpdatingProject: Boolean = false,
    val showEditSheet: Boolean = false,
    val error: Throwable? = null
)
