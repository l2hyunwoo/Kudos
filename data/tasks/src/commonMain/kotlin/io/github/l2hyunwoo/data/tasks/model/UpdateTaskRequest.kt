package io.github.l2hyunwoo.data.tasks.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Partial update payload for a task. Every field is nullable and defaults to null.
 *
 * The Edge Function applies `if (field !== undefined)`, so any field that should NOT
 * be touched must be ABSENT from the JSON (not present as null). This relies on the
 * shared Json being configured with `explicitNulls = false`, which omits null-valued
 * properties on encode. A status-only toggle therefore sends `{"status": "..."}`.
 */
@Serializable
data class UpdateTaskRequest(
    @SerialName("title")
    val title: String? = null,
    @SerialName("description")
    val description: String? = null,
    @SerialName("status")
    val status: TaskStatus? = null,
    @SerialName("priority")
    val priority: TaskPriority? = null,
    @SerialName("due_date")
    val dueDate: String? = null,
)
