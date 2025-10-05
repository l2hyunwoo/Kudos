package io.github.l2hyunwoo.data.tasks.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TaskStatus {
    @SerialName("todo")
    TODO,

    @SerialName("in_progress")
    IN_PROGRESS,

    @SerialName("done")
    DONE,

    @SerialName("backlog")
    BACKLOG
}
