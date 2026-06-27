package io.github.l2hyunwoo.data.tasks.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TaskStatus(val text: String, val fraction: Float) {
    @SerialName("todo")
    TODO("\uD83C\uDF13", 0.5f),

    @SerialName("in_progress")
    IN_PROGRESS("\uD83C\uDF14", 0.8f),

    @SerialName("done")
    DONE("\uD83C\uDF15", 1.0f),

    @SerialName("backlog")
    BACKLOG("\uD83C\uDF11", 0.0f),
}

// Waxing phase order used by both the list and detail screens: each tap advances one phase along the
// lit fraction. DONE wraps back to BACKLOG.
private val PhaseOrder = listOf(
    TaskStatus.BACKLOG,
    TaskStatus.TODO,
    TaskStatus.IN_PROGRESS,
    TaskStatus.DONE,
)

fun TaskStatus.next(): TaskStatus {
    val i = PhaseOrder.indexOf(this)
    return PhaseOrder[(i + 1) % PhaseOrder.size]
}
