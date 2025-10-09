package io.github.l2hyunwoo.data.categories.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateProjectRequest(
    @SerialName("title")
    val title: String,
    @SerialName("description")
    val description: String? = null
)
