package io.github.l2hyunwoo.tasks

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.core.design.component.moon.Moon
import io.github.l2hyunwoo.core.design.token.LunarDurationStandard
import io.github.l2hyunwoo.core.design.token.LunarStandardEasing
import io.github.l2hyunwoo.data.tasks.model.Task
import io.github.l2hyunwoo.data.tasks.model.TaskPriority
import io.github.l2hyunwoo.data.tasks.model.TaskStatus
import io.github.l2hyunwoo.data.tasks.model.fixture
import io.github.l2hyunwoo.data.tasks.model.next
import io.github.l2hyunwoo.kudos.core.common.compose.EventFlow
import io.github.l2hyunwoo.kudos.core.common.compose.rememberEventFlow
import io.github.l2hyunwoo.tasks.component.TaskRow
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableSharedFlow
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    uiState: TaskListUiState,
    eventFlow: EventFlow<TaskListEvent>,
    modifier: Modifier = Modifier,
    // Top contentPadding so the first row clears the glass header, which is hoisted to and owned by
    // the parent (MainScreen) OUTSIDE the backdrop recorder. The list spans the full screen and the
    // rest of its rows scroll UNDER the translucent header. Standalone usage passes 0.
    topContentPadding: Dp = 0.dp,
    onTaskClick: (Task) -> Unit = {},
    // Pull-to-refresh wiring. The gesture (Modifier.pullToRefresh) lives on the list INSIDE the
    // backdrop recorder, but the visible indicator is rendered by the parent (MainScreen) as a sibling
    // OUTSIDE the recorder so it stays crisp over the glass instead of being blurred. Null disables PTR
    // (standalone usage), in which case the list takes no pull modifier.
    pullToRefreshState: PullToRefreshState? = null,
    isRefreshing: Boolean = false,
    onRefresh: () -> Unit = {},
) {
    val groups = uiState.groups
    val isEmpty = groups.all { it.tasks.isEmpty() }
    val snackbarHostState = remember { SnackbarHostState() }

    // Flat lookup: task id -> (its group kind, its current priority). Built from the rendered groups so
    // drag onMove can decide same-group + read the drop neighbor's grade without re-deriving buckets.
    val rowIndex = remember(groups) {
        buildMap {
            groups.forEach { group ->
                group.tasks.forEach { task -> put(task.id, group.kind to task.priority) }
            }
        }
    }

    val lazyListState = rememberLazyListState()
    // Drag-and-drop reorder mapped to a priority change. Key-based (never raw index) so sticky headers
    // and spacers — whose keys are Strings, not task ids — are simply absent from rowIndex and ignored.
    val reorderState = rememberReorderableLazyListState(lazyListState) { from, to ->
        val fromId = from.key as? String ?: return@rememberReorderableLazyListState
        val toId = to.key as? String ?: return@rememberReorderableLazyListState
        val (fromGroup, _) = rowIndex[fromId] ?: return@rememberReorderableLazyListState
        val (toGroup, toPriority) = rowIndex[toId] ?: return@rememberReorderableLazyListState
        // Within-group only: dropping into another time-group would change the due date, which this
        // gesture must never do (option b). Cross-group drags are rejected as a no-op.
        if (fromGroup != toGroup) return@rememberReorderableLazyListState
        // Map the drop to the neighbor's grade. Same grade = no-op: intra-grade order isn't persisted,
        // the priority sort restores placement. taskId for the PATCH path is recoverable from the row.
        val target = groups.firstNotNullOfOrNull { g -> g.tasks.firstOrNull { it.id == fromId } }
            ?: return@rememberReorderableLazyListState
        if (target.priority == toPriority) return@rememberReorderableLazyListState
        eventFlow.tryEmit(TaskListEvent.ReorderPriority(target.taskId, target.id, toPriority))
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(message = error.message ?: "Something went wrong")
        }
    }

    // Optimistic swipe-delete with undo: confirm on dismiss, restore on action. Keyed on the pending
    // id so each new delete shows its own snackbar.
    LaunchedEffect(uiState.pendingDelete?.id) {
        if (uiState.pendingDelete != null) {
            val result = snackbarHostState.showSnackbar(
                message = "Task deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short,
            )
            when (result) {
                SnackbarResult.ActionPerformed -> eventFlow.tryEmit(TaskListEvent.UndoDelete)
                SnackbarResult.Dismissed -> eventFlow.tryEmit(TaskListEvent.ConfirmDelete)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        modifier = modifier,
    ) { scaffoldPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(scaffoldPadding)) {
            if (isEmpty) {
                EmptyState()
            } else {
                // The list is already inside MainScreen's single `Modifier.sky` recorder (this
                // screen draws no glass chrome of its own — the glass header is hoisted to
                // MainScreen, outside the recorder, so its foreground never pollutes the blur source).
                val listModifier = Modifier
                    .fillMaxSize()
                    // Pull-to-refresh gesture on the scroll container; the indicator is drawn by the
                    // parent outside the recorder (see param doc). Only attached when PTR is enabled.
                    .let { base ->
                        if (pullToRefreshState != null) {
                            base.pullToRefresh(
                                isRefreshing = isRefreshing,
                                state = pullToRefreshState,
                                onRefresh = onRefresh,
                            )
                        } else {
                            base
                        }
                    }
                    .padding(horizontal = 16.dp)
                LazyColumn(
                    state = lazyListState,
                    modifier = listModifier,
                    contentPadding = PaddingValues(
                        // Clear the glass header owned by the parent; rows beyond it scroll under it.
                        top = topContentPadding,
                        bottom = 16.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    groups.forEachIndexed { groupIndex, group ->
                        if (group.tasks.isEmpty()) return@forEachIndexed
                        // Sticky headers carry no drag handle: they stay inert and act as the group
                        // boundary the onMove guard rejects crossing.
                        stickyHeader(key = "header_${group.kind}") {
                            GroupHeader(kind = group.kind, count = group.tasks.size)
                        }
                        items(
                            count = group.tasks.size,
                            key = { taskIndex -> group.tasks[taskIndex].id },
                        ) { taskIndex ->
                            val task = group.tasks[taskIndex]
                            // Carry the existing fade+slide spec as the non-dragging placement animation
                            // so reordered rows still animate to their re-sorted slot.
                            val itemAnimation = Modifier.animateItem(
                                fadeInSpec = KudosTheme.motion.standard,
                                placementSpec = tween<IntOffset>(
                                    LunarDurationStandard,
                                    easing = LunarStandardEasing,
                                ),
                                fadeOutSpec = KudosTheme.motion.micro,
                            )
                            ReorderableItem(
                                state = reorderState,
                                key = task.id,
                                animateItemModifier = itemAnimation,
                            ) {
                                TaskRow(
                                    task = task,
                                    searchQuery = uiState.searchQuery,
                                    onClick = { onTaskClick(task) },
                                    // Tap the moon: advance one phase (DONE wraps to BACKLOG).
                                    onAdvanceStatus = {
                                        eventFlow.tryEmit(
                                            TaskListEvent.ChangeStatus(task.taskId, task.id, task.status.next()),
                                        )
                                    },
                                    // Swipe-right: mark done directly.
                                    onMarkDone = {
                                        eventFlow.tryEmit(
                                            TaskListEvent.ChangeStatus(task.taskId, task.id, TaskStatus.DONE),
                                        )
                                    },
                                    // Swipe-left: delete (undo snackbar).
                                    onDelete = {
                                        eventFlow.tryEmit(TaskListEvent.DeleteTask(task.taskId, task.id))
                                    },
                                    // Long-press to pick the row up; drag onto another grade re-grades it.
                                    modifier = Modifier.longPressDraggableHandle(),
                                )
                            }
                        }
                        if (groupIndex < groups.lastIndex) {
                            item(key = "spacer_${group.kind}") {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// Sticky section header for a time group: eyebrow label + count badge. Overdue leads in warning red
// (accent dot + red label/badge, never hidden) per DESIGN_SYSTEM_LUNAR 07-A; the rest read in ink3.
@Composable
private fun GroupHeader(kind: TaskGroupKind, count: Int) {
    val isOverdue = kind == TaskGroupKind.OVERDUE
    val accent = if (isOverdue) KudosTheme.colors.priority.urgent else KudosTheme.colors.ink.ink3
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (isOverdue) {
            // Red accent bar: the one section the spec says must shout.
            Box(
                modifier = Modifier
                    .size(width = 3.dp, height = 14.dp)
                    .clip(KudosTheme.shapes.pill)
                    .background(accent),
            )
        }
        Text(
            text = kind.label(),
            style = KudosTheme.typography.eyebrow,
            color = accent,
        )
        CountBadge(count = count, isOverdue = isOverdue)
    }
}

@Composable
private fun CountBadge(count: Int, isOverdue: Boolean) {
    val container = if (isOverdue) KudosTheme.colors.priority.urgent else KudosTheme.colors.surface.surface2
    val content = if (isOverdue) Color.White else KudosTheme.colors.ink.ink2
    Box(
        modifier = Modifier
            .clip(KudosTheme.shapes.pill)
            .background(container)
            .widthIn(min = 20.dp)
            .height(20.dp)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count.toString(),
            style = KudosTheme.typography.identifier,
            color = content,
        )
    }
}

// Korean section labels matching the app's tone (cf. "결과 없음"); spec prototype used English.
private fun TaskGroupKind.label(): String = when (this) {
    TaskGroupKind.OVERDUE -> "지남"
    TaskGroupKind.TODAY -> "오늘"
    TaskGroupKind.UPCOMING -> "예정"
    TaskGroupKind.NO_DUE -> "마감일 없음"
    TaskGroupKind.DONE -> "완료"
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // New moon: nothing illuminated — the empty-list glyph.
        Moon(k = 0f, size = 56.dp, modifier = Modifier.alpha(0.7f))
        Spacer(Modifier.height(16.dp))
        Text(
            text = "결과 없음",
            style = KudosTheme.typography.bodyLargeXB,
            color = KudosTheme.colors.ink.ink2,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskListScreenPreview() {
    // Span the buckets off a fixed "today" so the time grouping renders deterministically.
    val today = "2024-06-15"
    val sample = listOf(
        Task.fixture.copy(id = "o1", taskId = "kudos-1", title = "Overdue task", dueDate = "2024-06-10"),
        Task.fixture.copy(id = "t1", taskId = "kudos-2", title = "Today task", dueDate = today),
        Task.fixture.copy(id = "u1", taskId = "kudos-3", title = "Upcoming task", dueDate = "2024-06-20"),
        Task.fixture.copy(id = "n1", taskId = "kudos-4", title = "No due date", dueDate = null),
        Task.fixture.copy(id = "d1", taskId = "kudos-5", title = "Done task", status = TaskStatus.DONE),
    )
    KudosTheme {
        TaskListScreen(
            uiState = TaskListUiState(groups = groupTasksByDueDate(sample, today)),
            eventFlow = rememberEventFlow(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskListScreenEmptyPreview() {
    KudosTheme {
        TaskListScreen(
            uiState = TaskListUiState(categories = persistentListOf()),
            eventFlow = MutableSharedFlow(),
        )
    }
}
