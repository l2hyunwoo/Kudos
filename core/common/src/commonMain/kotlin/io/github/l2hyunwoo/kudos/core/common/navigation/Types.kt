package io.github.l2hyunwoo.kudos.core.common.navigation

import kotlinx.serialization.Serializable

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
