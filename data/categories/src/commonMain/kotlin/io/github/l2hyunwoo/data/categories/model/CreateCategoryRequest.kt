package io.github.l2hyunwoo.data.categories.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateCategoryRequest(
    @SerialName("prefix")
    val prefix: String,
    @SerialName("title")
    val title: String,
    @SerialName("color")
    val color: String? = null
)
