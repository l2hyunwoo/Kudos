package io.github.l2hyunwoo.data.tasks.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TaskStatus(val text: String) {
    @SerialName("todo")
    TODO("To Do"),

    @SerialName("in_progress")
    IN_PROGRESS("In Progress"),

    @SerialName("done")
    DONE("Done"),

    @SerialName("backlog")
    BACKLOG("Backlog"),
}
