package io.github.l2hyunwoo.project

import io.github.l2hyunwoo.data.categories.model.UpdateProjectRequest

sealed interface ProjectDetailEvent {
    data object ShowEditSheet : ProjectDetailEvent
    data object DismissEditSheet : ProjectDetailEvent
    data class UpdateProject(val request: UpdateProjectRequest) : ProjectDetailEvent
    data object NavigateBack : ProjectDetailEvent
}
