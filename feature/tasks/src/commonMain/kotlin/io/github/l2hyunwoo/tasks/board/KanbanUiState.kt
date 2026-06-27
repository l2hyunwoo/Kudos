package io.github.l2hyunwoo.tasks.board

import androidx.compose.runtime.Immutable
import io.github.l2hyunwoo.data.tasks.model.Task
import io.github.l2hyunwoo.data.tasks.model.TaskStatus
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@Immutable
data class KanbanColumn(
    val status: TaskStatus,
    val tasks: ImmutableList<Task>,
)

@Immutable
data class KanbanUiState(
    val columns: ImmutableList<KanbanColumn> = persistentListOf(),
    val isLoading: Boolean = false,
    val error: Throwable? = null,
)
