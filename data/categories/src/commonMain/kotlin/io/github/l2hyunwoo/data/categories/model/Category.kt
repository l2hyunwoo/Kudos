package io.github.l2hyunwoo.data.categories.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Category(
    @SerialName("id")
    val id: String,
    @SerialName("prefix")
    val prefix: String,
    @SerialName("title")
    val title: String,
    @SerialName("color")
    val color: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("projects")
    val projects: List<Project>
)
