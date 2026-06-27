package io.github.l2hyunwoo.tasks.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import io.github.l2hyunwoo.core.design.component.sheet.KudosBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.skydoves.cloudy.rememberSky
import com.skydoves.cloudy.sky
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.core.design.component.moon.Moon
import io.github.l2hyunwoo.core.design.component.moon.MoonProgress
import io.github.l2hyunwoo.core.design.component.moon.MoonToggle
import io.github.l2hyunwoo.core.design.component.surface.glassSurface
import io.github.l2hyunwoo.core.design.token.LunarDurationMicro
import io.github.l2hyunwoo.core.design.token.LunarStandardEasing
import io.github.l2hyunwoo.data.tasks.model.TaskPriority
import io.github.l2hyunwoo.data.tasks.model.TaskStatus
import io.github.l2hyunwoo.data.tasks.model.next
import io.github.l2hyunwoo.kudos.core.common.compose.EventFlow
import io.github.l2hyunwoo.tasks.detail.component.EditTaskBottomSheet
import kotlinx.collections.immutable.ImmutableList

// Phase order for the picker sheet (waxing). The single-step advance uses TaskStatus.next() from the
// data model, shared with the list screen.
private val PhaseOrder = listOf(
    TaskStatus.BACKLOG,
    TaskStatus.TODO,
    TaskStatus.IN_PROGRESS,
    TaskStatus.DONE,
)

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
        // `sky` records this region as the glass top bar's blur backdrop. Keep it on the static
        // outer Box, not on the scrolling Column: a `sky` recorder on the scroll container itself
        // stops the container from consuming vertical drags (the list/page stops scrolling). The Box
        // is a non-scrolling parent, so the recorded backdrop is identical while the Column scrolls.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .sky(sky)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                    onToggle = { subtask -> eventFlow.tryEmit(TaskDetailEvent.ToggleSubtask(subtask)) },
                    onDelete = { subtask -> eventFlow.tryEmit(TaskDetailEvent.DeleteSubtask(subtask)) },
                    onAdd = { title -> eventFlow.tryEmit(TaskDetailEvent.CreateSubtask(title)) },
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
        // Cross-fade the hero label when the status changes instead of snapping. standard is a Float
        // spec, so reduce-motion collapses it for free.
        Crossfade(
            targetState = status,
            animationSpec = KudosTheme.motion.standard,
            label = "statusLabel",
        ) { phase ->
            Text(
                text = phase.label(),
                style = KudosTheme.typography.titleMediumB,
                color = KudosTheme.colors.ink.ink,
            )
        }
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
    // Flip true on first composition so the rows animate in once; the per-row enter delay (below)
    // produces the stagger. Held as state so it survives recomposition during the sheet's lifetime.
    var rowsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { rowsVisible = true }
    KudosBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
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
            PhaseOrder.forEachIndexed { index, status ->
                PhasePickerRow(
                    status = status,
                    isSelected = status == selected,
                    visible = rowsVisible,
                    // Light stagger: each row trails the previous by one micro step. Easing/duration
                    // come from the Lunar tokens; only the per-index delay is computed here.
                    enterDelayMillis = index * PhaseStaggerStepMillis,
                    onClick = { onSelect(status) },
                )
            }
        }
    }
}

// One micro step between staggered phase rows; kept small so 4 rows settle inside ~one standard beat.
private const val PhaseStaggerStepMillis = LunarDurationMicro / 2

@Composable
private fun PhasePickerRow(
    status: TaskStatus,
    isSelected: Boolean,
    visible: Boolean,
    enterDelayMillis: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Quick fade + small upward slide as each row enters, offset per row for the stagger. The exit is
    // a no-op (the sheet itself slides away), so we only animate the appearance.
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = LunarDurationMicro,
                delayMillis = enterDelayMillis,
                easing = LunarStandardEasing,
            ),
        ) + slideInVertically(
            animationSpec = tween(
                durationMillis = LunarDurationMicro,
                delayMillis = enterDelayMillis,
                easing = LinearOutSlowInEasing,
            ),
            initialOffsetY = { it / 4 },
        ),
        exit = fadeOut(animationSpec = tween(LunarDurationMicro)),
    ) {
        PhasePickerRowContent(
            status = status,
            isSelected = isSelected,
            onClick = onClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun PhasePickerRowContent(
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

// Subtask section: a progress ring driven by real done/total, then the child rows, then an inline
// add row. The ring's fraction (done/total) is the same value that would advance the parent moon as
// children complete. Tapping a row's moon toggles DONE↔TODO; the trailing icon deletes it.
@Composable
private fun SubtaskSection(
    subtasks: ImmutableList<SubtaskItem>,
    done: Int,
    total: Int,
    onToggle: (SubtaskItem) -> Unit,
    onDelete: (SubtaskItem) -> Unit,
    onAdd: (String) -> Unit,
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(KudosTheme.shapes.card)
                .background(KudosTheme.colors.surface.surface2)
                .padding(vertical = 4.dp),
        ) {
            if (subtasks.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(20.dp),
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
            } else {
                subtasks.forEach { subtask ->
                    SubtaskRow(
                        subtask = subtask,
                        onToggle = { onToggle(subtask) },
                        onDelete = { onDelete(subtask) },
                    )
                }
            }
            AddSubtaskRow(onAdd = onAdd)
        }
    }
}

@Composable
private fun SubtaskRow(
    subtask: SubtaskItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDone = subtask.status == TaskStatus.DONE
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Tap the moon to flip DONE↔TODO; the fill tweens between phases.
        MoonToggle(
            k = subtask.status.fraction,
            onTap = onToggle,
            onLongPress = onToggle,
            size = 22.dp,
        )
        Text(
            text = subtask.title,
            style = KudosTheme.typography.rowTitle,
            color = if (isDone) KudosTheme.colors.ink.ink3 else KudosTheme.colors.ink.ink,
            textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Delete subtask",
                tint = KudosTheme.colors.ink.ink3,
            )
        }
    }
}

// Inline add affordance: a moon glyph + text field + add action. Submitting (IME Done or the add
// button) emits the title and clears the field so several subtasks can be added in a row.
@Composable
private fun AddSubtaskRow(
    onAdd: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var draft by remember { mutableStateOf("") }
    val canAdd = draft.isNotBlank()
    val submit = {
        if (draft.isNotBlank()) {
            onAdd(draft.trim())
            draft = ""
        }
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Moon(k = 0f, size = 22.dp)
        OutlinedTextField(
            value = draft,
            onValueChange = { draft = it },
            placeholder = {
                Text(
                    text = "서브태스크 추가",
                    style = KudosTheme.typography.rowTitle,
                    color = KudosTheme.colors.ink.ink3,
                )
            },
            singleLine = true,
            shape = KudosTheme.shapes.chipSmall,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = KudosTheme.colors.brand.primary600,
                unfocusedBorderColor = KudosTheme.colors.surface.outlineStrong,
                cursorColor = KudosTheme.colors.brand.primary600,
            ),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { submit() }),
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = submit, enabled = canAdd) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add subtask",
                tint = if (canAdd) KudosTheme.colors.brand.primary600 else KudosTheme.colors.ink.ink3,
            )
        }
    }
}

private fun TaskStatus.label(): String = when (this) {
    TaskStatus.BACKLOG -> "백로그"
    TaskStatus.TODO -> "할 일"
    TaskStatus.IN_PROGRESS -> "진행 중"
    TaskStatus.DONE -> "완료"
}
