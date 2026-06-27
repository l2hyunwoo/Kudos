package io.github.l2hyunwoo.tasks

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
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
import io.github.l2hyunwoo.data.tasks.model.TasksResponse
import io.github.l2hyunwoo.data.tasks.model.fixture
import io.github.l2hyunwoo.data.tasks.model.next
import io.github.l2hyunwoo.kudos.core.common.compose.EventFlow
import io.github.l2hyunwoo.kudos.core.common.compose.rememberEventFlow
import io.github.l2hyunwoo.tasks.component.TaskRow
import kotlinx.collections.immutable.ImmutableList
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
    val categories = uiState.categories
    val isEmpty = categories.all { it.tasks.isEmpty() }
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
                    categories.forEachIndexed { categoryIndex, category ->
                        if (category.tasks.isEmpty()) return@forEachIndexed
                        stickyHeader(key = "header_${category.id}") {
                            Text(
                                text = category.title.uppercase(),
                                style = KudosTheme.typography.eyebrow,
                                color = KudosTheme.colors.ink.ink3,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                            )
                        }
                        items(
                            count = category.tasks.size,
                            key = { taskIndex -> category.tasks[taskIndex].id },
                        ) { taskIndex ->
                            val task = category.tasks[taskIndex]
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
                        if (categoryIndex < categories.lastIndex) {
                            item(key = "spacer_${category.id}") {
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
    KudosTheme {
        TaskListScreen(
            uiState = TaskListUiState(
                categories = persistentListOf(TasksResponse.CategoryWithTasks.fixture),
            ),
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
