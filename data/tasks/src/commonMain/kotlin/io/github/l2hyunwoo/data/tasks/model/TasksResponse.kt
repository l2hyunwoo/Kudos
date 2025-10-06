package io.github.l2hyunwoo.data.tasks.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TasksResponse(
    @SerialName("categories")
    val categories: List<CategoryWithTasks>
) {
    @Serializable
    data class CategoryWithTasks(
        @SerialName("id")
        val id: String,
        @SerialName("prefix")
        val prefix: String,
        @SerialName("color")
        val color: String,
        @SerialName("title")
        val title: String,
        @SerialName("created_at")
        val createdAt: String,
        @SerialName("updated_at")
        val updatedAt: String,
        @SerialName("tasks")
        val tasks: List<Task>
    )
}

val TasksResponse.CategoryWithTasks.Companion.fixture
    get() = TasksResponse.CategoryWithTasks(
        id = "category-1",
        prefix = "CAT1",
        color = "#FF5733",
        title = "Sample Category",
        createdAt = "2024-01-01T00:00:00Z",
        updatedAt = "2024-01-02T00:00:00Z",
        tasks = listOf(Task.fixture)
    )

