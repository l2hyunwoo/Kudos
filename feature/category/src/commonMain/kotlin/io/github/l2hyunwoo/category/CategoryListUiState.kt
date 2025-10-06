package io.github.l2hyunwoo.category

import io.github.l2hyunwoo.data.categories.model.Category
import kotlinx.collections.immutable.ImmutableList

data class CategoryListUiState(
    val categories: ImmutableList<Category>,
    val isLoading: Boolean = false,
    val error: Throwable? = null
)
