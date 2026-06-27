package io.github.l2hyunwoo.kudos.core.common.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Main

@Serializable
data object TaskList

@Serializable
data object CategoryList

@Serializable
data class ProjectDetail(
    val projectId: String,
    val categoryId: String,
    val title: String,
    val description: String?,
    val categoryColor: String,
    val categoryPrefix: String
)

@Serializable
data class TaskDetail(
    // UUID id, used to locate the task in the cache for optimistic delete.
    val id: String,
    // task_id text identifier (e.g. "KUDOS-1"), used as the PATCH/DELETE path param.
    val taskId: String,
    val title: String,
    val description: String?,
    val status: String,
    val priority: String,
    val dueDate: String?,
)
