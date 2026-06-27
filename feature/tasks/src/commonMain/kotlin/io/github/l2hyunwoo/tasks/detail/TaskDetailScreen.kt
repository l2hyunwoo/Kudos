package io.github.l2hyunwoo.tasks.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.skydoves.cloudy.rememberSky
import com.skydoves.cloudy.sky
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.core.design.component.moon.Moon
import io.github.l2hyunwoo.core.design.component.moon.MoonProgress
import io.github.l2hyunwoo.core.design.component.moon.MoonToggle
import io.github.l2hyunwoo.core.design.component.surface.glassSurface
import io.github.l2hyunwoo.data.tasks.model.TaskPriority
import io.github.l2hyunwoo.data.tasks.model.TaskStatus
import io.github.l2hyunwoo.kudos.core.common.compose.EventFlow
import io.github.l2hyunwoo.tasks.detail.component.EditTaskBottomSheet
import kotlinx.collections.immutable.ImmutableList

// Advances the status one phase along the fraction order (waxing). DONE wraps back to BACKLOG.
private val PhaseOrder = listOf(
    TaskStatus.BACKLOG,
    TaskStatus.TODO,
    TaskStatus.IN_PROGRESS,
    TaskStatus.DONE,
)

private fun TaskStatus.next(): TaskStatus {
    val i = PhaseOrder.indexOf(this)
    return PhaseOrder[(i + 1) % PhaseOrder.size]
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    uiState: TaskDetailUiState,
    eventFlow: EventFlow<TaskDetailEvent>,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val sky = rememberSky()
    var showPhasePicker by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(message = error.message ?: "Something went wrong")
        }
    }

    // Optimistic delete with undo: confirm on dismiss, restore on action.
    LaunchedEffect(uiState.pendingDelete) {
        if (uiState.pendingDelete) {
            val result = snackbarHostState.showSnackbar(
                message = "Task deleted",
                actionLabel = "Undo",
                duration = SnackbarDuration.Short
            )
            when (result) {
                SnackbarResult.ActionPerformed -> eventFlow.tryEmit(TaskDetailEvent.UndoDelete)
                SnackbarResult.Dismissed -> eventFlow.tryEmit(TaskDetailEvent.ConfirmDelete)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(uiState.taskId, style = KudosTheme.typography.identifier)
                },
                navigationIcon = {
                    IconButton(onClick = { eventFlow.tryEmit(TaskDetailEvent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = KudosTheme.colors.ink.ink,
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { eventFlow.tryEmit(TaskDetailEvent.ShowEditSheet) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = KudosTheme.colors.ink.ink2,
                        )
                    }
                    IconButton(onClick = { eventFlow.tryEmit(TaskDetailEvent.RequestDelete) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = KudosTheme.colors.priority.urgent,
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.glassSurface(sky = sky, shape = KudosTheme.shapes.card),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = KudosTheme.colors.surface.bg,
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .sky(sky)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                PriorityRow(uiState.priority)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = uiState.title,
                    style = KudosTheme.typography.titleLargeB,
                    color = KudosTheme.colors.ink.ink,
                    textDecoration = if (uiState.status == TaskStatus.DONE) {
                        TextDecoration.LineThrough
                    } else {
                        TextDecoration.None
                    },
                )

                uiState.description?.let { description ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = description,
                        style = KudosTheme.typography.bodyMediumR,
                        color = KudosTheme.colors.ink.ink2,
                    )
                }

                uiState.dueDate?.let { due ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Due: $due",
                        style = KudosTheme.typography.bodySmallR,
                        color = KudosTheme.colors.ink.ink3,
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                StatusHero(
                    status = uiState.status,
                    onAdvance = {
                        eventFlow.tryEmit(TaskDetailEvent.ChangeStatus(uiState.status.next()))
                    },
                    onOpenPicker = { showPhasePicker = true },
                )

                Spacer(modifier = Modifier.height(32.dp))

                SubtaskSection(
                    subtasks = uiState.subtasks,
                    done = uiState.subtaskDone,
                    total = uiState.subtaskTotal,
                )
            }

            if (uiState.isMutating) {
                CircularProgressIndicator(
                    color = KudosTheme.colors.brand.primary600,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }

    if (showPhasePicker) {
        PhasePickerSheet(
            selected = uiState.status,
            onSelect = { status ->
                eventFlow.tryEmit(TaskDetailEvent.ChangeStatus(status))
                showPhasePicker = false
            },
            onDismiss = { showPhasePicker = false },
        )
    }

    if (uiState.showEditSheet) {
        EditTaskBottomSheet(
            initialTitle = uiState.title,
            initialDescription = uiState.description,
            initialStatus = uiState.status,
            initialPriority = uiState.priority,
            initialDueDate = uiState.dueDate,
            onDismiss = { eventFlow.tryEmit(TaskDetailEvent.DismissEditSheet) },
            onUpdate = { request -> eventFlow.tryEmit(TaskDetailEvent.UpdateTask(request)) }
        )
    }
}

// Priority as a quiet left bar + pastel-tinted label, not a loud filled chip.
@Composable
private fun PriorityRow(priority: TaskPriority, modifier: Modifier = Modifier) {
    val p = KudosTheme.colors.priority
    val dot = when (priority) {
        TaskPriority.URGENT -> p.urgent
        TaskPriority.HIGH -> p.high
        TaskPriority.MEDIUM -> p.medium
        TaskPriority.LOW -> p.low
    }
    val (bg, fg) = KudosTheme.colors.pastelChip(dot)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(16.dp)
                .clip(KudosTheme.shapes.pill)
                .background(dot),
        )
        Text(
            text = priority.text,
            style = KudosTheme.typography.labelSmallM,
            color = fg,
            modifier = Modifier
                .clip(KudosTheme.shapes.chipSmall)
                .background(bg)
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
    }
}

@Composable
private fun StatusHero(
    status: TaskStatus,
    onAdvance: () -> Unit,
    onOpenPicker: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "STATUS",
            style = KudosTheme.typography.eyebrow,
            color = KudosTheme.colors.ink.ink3,
        )
        MoonToggle(
            k = status.fraction,
            onTap = onAdvance,
            onLongPress = onOpenPicker,
            size = 64.dp,
        )
        Text(
            text = status.label(),
            style = KudosTheme.typography.titleMediumB,
            color = KudosTheme.colors.ink.ink,
        )
        Text(
            text = "탭하여 다음 단계로 · 길게 눌러 선택",
            style = KudosTheme.typography.bodySmallR,
            color = KudosTheme.colors.ink.ink3,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PhasePickerSheet(
    selected: TaskStatus,
    onSelect: (TaskStatus) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = KudosTheme.shapes.sheet,
        containerColor = KudosTheme.colors.surface.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = "상태 선택",
                style = KudosTheme.typography.titleMediumB,
                color = KudosTheme.colors.ink.ink,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            PhaseOrder.forEach { status ->
                PhasePickerRow(
                    status = status,
                    isSelected = status == selected,
                    onClick = { onSelect(status) },
                )
            }
        }
    }
}

@Composable
private fun PhasePickerRow(
    status: TaskStatus,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(KudosTheme.shapes.row)
            .background(
                if (isSelected) KudosTheme.colors.brand.primary050 else Color.Transparent,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Moon(k = status.fraction, size = 32.dp)
        Text(
            text = status.label(),
            style = KudosTheme.typography.rowTitle,
            color = if (isSelected) KudosTheme.colors.brand.primary600 else KudosTheme.colors.ink.ink,
        )
    }
}

// Subtask section: a progress ring driven by real done/total, then the child rows. The ring's
// fraction (done/total) is the same value that would advance the parent moon as children complete.
@Composable
private fun SubtaskSection(
    subtasks: ImmutableList<SubtaskItem>,
    done: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "SUBTASKS",
                style = KudosTheme.typography.eyebrow,
                color = KudosTheme.colors.ink.ink3,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (total > 0) {
                    Text(
                        text = "$done / $total",
                        style = KudosTheme.typography.labelLargeM,
                        color = KudosTheme.colors.ink.ink2,
                    )
                }
                MoonProgress(done = done, total = total, size = 24.dp)
            }
        }
        if (subtasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(KudosTheme.shapes.card)
                    .background(KudosTheme.colors.surface.surface2)
                    .padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Moon(k = 0f, size = 36.dp)
                    Text(
                        text = "하위 작업이 아직 없어요",
                        style = KudosTheme.typography.bodyMediumR,
                        color = KudosTheme.colors.ink.ink3,
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(KudosTheme.shapes.card)
                    .background(KudosTheme.colors.surface.surface2)
                    .padding(vertical = 4.dp),
            ) {
                subtasks.forEach { subtask ->
                    SubtaskRow(subtask)
                }
            }
        }
    }
}

@Composable
private fun SubtaskRow(subtask: SubtaskItem, modifier: Modifier = Modifier) {
    val isDone = subtask.status == TaskStatus.DONE
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Moon(k = subtask.status.fraction, size = 22.dp)
        Text(
            text = subtask.title,
            style = KudosTheme.typography.rowTitle,
            color = if (isDone) KudosTheme.colors.ink.ink3 else KudosTheme.colors.ink.ink,
            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
        )
    }
}

private fun TaskStatus.label(): String = when (this) {
    TaskStatus.BACKLOG -> "백로그"
    TaskStatus.TODO -> "할 일"
    TaskStatus.IN_PROGRESS -> "진행 중"
    TaskStatus.DONE -> "완료"
}
