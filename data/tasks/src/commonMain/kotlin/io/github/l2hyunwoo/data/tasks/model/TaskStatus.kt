package io.github.l2hyunwoo.data.tasks.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TaskStatus(val text: String) {
    @SerialName("todo")
    TODO("\uD83C\uDF13"),

    @SerialName("in_progress")
    IN_PROGRESS("\uD83C\uDF14"),

    @SerialName("done")
    DONE("\uD83C\uDF15"),

    @SerialName("backlog")
    BACKLOG("\uD83C\uDF11"),
}
