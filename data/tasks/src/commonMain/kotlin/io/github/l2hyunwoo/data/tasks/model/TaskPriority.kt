package io.github.l2hyunwoo.data.tasks.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TaskPriority(
    val text: String
) {
    @SerialName("urgent")
    URGENT("Urgent"),

    @SerialName("high")
    HIGH("High"),

    @SerialName("medium")
    MEDIUM("Medium"),

    @SerialName("low")
    LOW("Low"),
}
