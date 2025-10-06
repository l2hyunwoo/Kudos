package io.github.l2hyunwoo.data.tasks.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Task(
    @SerialName("id")
    val id: String,
    @SerialName("task_id")
    val taskId: String,
    @SerialName("task_number")
    val taskNumber: Int,
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String? = null,
    @SerialName("status")
    val status: TaskStatus,
    @SerialName("priority")
    val priority: TaskPriority,
    @SerialName("due_date")
    val dueDate: String? = null,
    @SerialName("project_id")
    val projectId: String? = null,
    @SerialName("project_title")
    val projectTitle: String? = null,
    @SerialName("parent_task_id")
    val parentTaskId: String? = null,
    @SerialName("display_order")
    val displayOrder: Int,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String
)

val Task.Companion.fixture
    get() = Task(
        id = "1",
        taskId = "task-1",
        taskNumber = 1,
        title = "Sample Task",
        description = "This is a sample task description.",
        status = TaskStatus.TODO,
        priority = TaskPriority.MEDIUM,
        dueDate = "2024-12-31",
        projectId = "project-1",
        projectTitle = "Sample Project",
        parentTaskId = null,
        displayOrder = 1,
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-02T00:00:00Z"
    )
