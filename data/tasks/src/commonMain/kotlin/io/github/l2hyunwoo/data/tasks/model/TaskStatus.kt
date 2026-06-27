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
