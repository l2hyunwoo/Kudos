package io.github.l2hyunwoo.data.tasks.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateTaskRequest(
    @SerialName("category_id")
    val categoryId: String,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("project_id")
    val projectId: String? = null,
    @SerialName("priority")
    val priority: TaskPriority = TaskPriority.MEDIUM,
    @SerialName("status")
    val status: TaskStatus = TaskStatus.TODO,
    @SerialName("due_date")
    val dueDate: String? = null
)
