package io.github.l2hyunwoo.data.tasks.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProjectTasksResponse(
    @SerialName("tasks")
    val tasks: List<Task>
)
