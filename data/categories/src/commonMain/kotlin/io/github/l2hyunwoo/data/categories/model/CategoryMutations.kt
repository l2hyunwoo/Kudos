package io.github.l2hyunwoo.data.categories.model

import soil.query.MutationKey
import soil.query.SubscriptionKey

typealias CreateCategoryMutationKey = MutationKey<CategoriesResponse, CreateCategoryRequest>
typealias DeleteCategoryMutationKey = MutationKey<CategoriesResponse, String>
typealias CreateProjectMutationKey = MutationKey<CategoriesResponse, CreateProjectParams>
typealias DeleteProjectMutationKey = MutationKey<CategoriesResponse, DeleteProjectParams>
typealias CategoriesSubscriptionKey = SubscriptionKey<List<Category>>

data class CreateProjectParams(
    val categoryId: String,
    val request: CreateProjectRequest
)

data class DeleteProjectParams(
    val categoryId: String,
    val projectId: String
)
