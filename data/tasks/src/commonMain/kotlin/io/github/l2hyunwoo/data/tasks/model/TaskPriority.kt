package io.github.l2hyunwoo.data.tasks.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TaskPriority(
    val text: String,
    val color: Long
) {
    @SerialName("urgent")
    URGENT("Urgent", 0xFFE53E3E),

    @SerialName("high")
    HIGH("High", 0xFFFFA500),

    @SerialName("medium")
    MEDIUM("Medium", 0xFFFDD835),

    @SerialName("low")
    LOW("Low", 0xFF4CAF50),
}
