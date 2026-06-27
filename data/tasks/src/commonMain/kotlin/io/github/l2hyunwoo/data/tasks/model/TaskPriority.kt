package io.github.l2hyunwoo.data.tasks.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TaskPriority(
    val text: String,
    val color: Long
) {
    @SerialName("urgent")
    URGENT("Urgent", 0xFFF2555A),

    @SerialName("high")
    HIGH("High", 0xFFF2994A),

    @SerialName("medium")
    MEDIUM("Medium", 0xFFEAC44E),

    @SerialName("low")
    LOW("Low", 0xFF54C08A),
}
