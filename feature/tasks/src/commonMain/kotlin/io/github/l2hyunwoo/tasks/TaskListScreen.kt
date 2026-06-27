package io.github.l2hyunwoo.tasks

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.skydoves.cloudy.Sky
import com.skydoves.cloudy.rememberSky
import com.skydoves.cloudy.sky
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.core.design.component.moon.Moon
import io.github.l2hyunwoo.core.design.component.surface.glassSurface
import io.github.l2hyunwoo.core.design.token.LunarDurationStandard
import io.github.l2hyunwoo.core.design.token.LunarStandardEasing
import io.github.l2hyunwoo.data.tasks.model.Task
import io.github.l2hyunwoo.data.tasks.model.TaskStatus
import io.github.l2hyunwoo.data.tasks.model.fixture
import io.github.l2hyunwoo.data.tasks.model.next
import io.github.l2hyunwoo.kudos.core.common.compose.EventFlow
import io.github.l2hyunwoo.kudos.core.common.compose.rememberEventFlow
import io.github.l2hyunwoo.tasks.component.TaskRow
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kudos.feature.tasks.generated.resources.Res
import kudos.feature.tasks.generated.resources.tasks
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    uiState: TaskListUiState,
    eventFlow: EventFlow<TaskListEvent>,
    modifier: Modifier = Modifier,
    // Reuse the backdrop recorder hoisted by the parent (MainScreen) so a single sky records+blurs
    // per visible screen. The default makes standalone usage (own nav route) self-contained.
    sky: Sky = rememberSky(),
    onTaskClick: (Task) -> Unit = {},
) {
    val groups = uiState.groups
    val isEmpty = groups.all { it.tasks.isEmpty() }
    val snackbarHostState = remember { SnackbarHostState() }

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
                val statusBarTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .sky(sky)
                        .padding(horizontal = 16.dp),
                    contentPadding = PaddingValues(
                        // Clear the floating glass top bar (status bar + bar height + its vertical padding).
                        top = statusBarTop + TopBarHeight + 24.dp,
                        bottom = 16.dp,
                    ),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    groups.forEachIndexed { groupIndex, group ->
                        if (group.tasks.isEmpty()) return@forEachIndexed
                        stickyHeader(key = "header_${group.kind}") {
                            GroupHeader(kind = group.kind, count = group.tasks.size)
                        }
                        items(
                            count = group.tasks.size,
                            key = { taskIndex -> group.tasks[taskIndex].id },
                        ) { taskIndex ->
                            val task = group.tasks[taskIndex]
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
                                // Filtered/reordered rows fade+slide instead of snapping. fade specs are
                                // Float (reduce-motion free); placement is rebuilt as an IntOffset spec.
                                modifier = Modifier.animateItem(
                                    fadeInSpec = KudosTheme.motion.standard,
                                    placementSpec = tween<IntOffset>(
                                        LunarDurationStandard,
                                        easing = LunarStandardEasing,
                                    ),
                                    fadeOutSpec = KudosTheme.motion.micro,
                                ),
                            )
                        }
                        if (groupIndex < groups.lastIndex) {
                            item(key = "spacer_${group.kind}") {
                                Spacer(modifier = Modifier.height(16.dp))
                            }
                        }
                    }
                }
            }

            GlassTopBar(
                title = stringResource(Res.string.tasks),
                sky = sky,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

private val TopBarHeight = 56.dp

@Composable
private fun GlassTopBar(
    title: String,
    sky: com.skydoves.cloudy.Sky,
    modifier: Modifier = Modifier,
) {
    val topInset = WindowInsets.statusBars.asPaddingValues()
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = topInset.run { calculateTopPadding() })
            .padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(TopBarHeight)
                .glassSurface(sky = sky, shape = KudosTheme.shapes.card),
            contentAlignment = Alignment.CenterStart,
        ) {
            Text(
                text = title,
                style = KudosTheme.typography.bodyLargeXB,
                color = KudosTheme.colors.ink.ink,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
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
