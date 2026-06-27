package io.github.l2hyunwoo.tasks.board

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mohamedrejeb.compose.dnd.DragAndDropContainer
import com.mohamedrejeb.compose.dnd.drag.DraggableItem
import com.mohamedrejeb.compose.dnd.drop.dropTarget
import com.mohamedrejeb.compose.dnd.rememberDragAndDropState
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.core.design.component.moon.Moon
import io.github.l2hyunwoo.core.design.token.LunarDurationStandard
import io.github.l2hyunwoo.core.design.token.LunarStandardEasing
import io.github.l2hyunwoo.data.tasks.model.Task
import io.github.l2hyunwoo.data.tasks.model.TaskStatus
import io.github.l2hyunwoo.data.tasks.model.fixture
import io.github.l2hyunwoo.kudos.core.common.compose.EventFlow
import io.github.l2hyunwoo.kudos.core.common.compose.rememberEventFlow
import io.github.l2hyunwoo.tasks.TaskListEvent
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

// Board view of the same task data as the list. Each of the four status columns is a drop target; a
// card is a draggable. Dropping a card on a different column optimistically moves it (presenter sets a
// statusOverride) and emits ChangeStatus, which the presenter commits + rolls back on failure. The
// glass header/nav stay outside this subtree (rendered by MainScreen as siblings of the recorder).
@Composable
fun KanbanBoard(
    uiState: KanbanUiState,
    eventFlow: EventFlow<TaskListEvent>,
    modifier: Modifier = Modifier,
    topContentPadding: Dp = 0.dp,
    onTaskClick: (Task) -> Unit = {},
) {
    // T = Task: the dragged payload carries the moved task. Long-press to lift so a vertical scroll in
    // a column isn't hijacked by the drag.
    val dndState = rememberDragAndDropState<Task>(dragAfterLongPress = true)

    DragAndDropContainer(
        state = dndState,
        modifier = modifier.fillMaxSize(),
    ) {
        // BoxWithConstraints (commonMain) measures the available width so each column is ~78% of it and
        // the next column peeks. LocalConfiguration/screenWidthDp is Android-only, so it can't be used
        // in commonMain.
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val columnWidth = maxWidth * 0.78f
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                uiState.columns.forEach { column ->
                    KanbanColumnView(
                        column = column,
                        width = columnWidth,
                        topContentPadding = topContentPadding,
                        isHovered = dndState.hoveredDropTargetKey == column.status,
                        dndModifier = Modifier.dropTarget(
                            key = column.status,
                            state = dndState,
                            onDrop = { dragged ->
                                val task = dragged.data
                                // Same-column drop is a no-op; only a real status change is emitted.
                                if (task.status != column.status) {
                                    eventFlow.tryEmit(
                                        TaskListEvent.ChangeStatus(task.taskId, task.id, column.status),
                                    )
                                }
                            },
                        ),
                    ) { task ->
                        DraggableItem(
                            key = task.id,
                            data = task,
                            state = dndState,
                        ) {
                            // Lift: scale up + shadow while dragging, read inside graphicsLayer
                            // (draw phase). scale animates via a Float spec so reduce-motion collapses it.
                            val scale by animateFloatAsState(
                                targetValue = if (isDragging) 1.04f else 1f,
                                animationSpec = KudosTheme.motion.micro,
                                label = "kanbanLift",
                            )
                            KanbanCard(
                                task = task,
                                onClick = { onTaskClick(task) },
                                modifier = Modifier
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                    }
                                    .shadow(
                                        elevation = if (isDragging) 8.dp else 0.dp,
                                        shape = KudosTheme.shapes.row,
                                        ambientColor = KudosTheme.colors.glass.shadowTint,
                                        spotColor = KudosTheme.colors.glass.shadowTint,
                                    ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KanbanColumnView(
    column: KanbanColumn,
    width: Dp,
    topContentPadding: Dp,
    isHovered: Boolean,
    dndModifier: Modifier,
    cardContent: @Composable (Task) -> Unit,
) {
    val colorSpec = tween<Color>(LunarDurationStandard, easing = LunarStandardEasing)
    // Target glow: tint the column fill + border while a card hovers over it.
    val borderColor by animateColorAsState(
        targetValue = if (isHovered) KudosTheme.colors.brand.primary400 else Color.Transparent,
        animationSpec = colorSpec,
    )
    val bgColor by animateColorAsState(
        targetValue = if (isHovered) KudosTheme.colors.brand.primary050 else KudosTheme.colors.surface.surface2,
        animationSpec = colorSpec,
    )
    Column(
        modifier = Modifier
            .width(width)
            .fillMaxHeight()
            .padding(top = topContentPadding, bottom = 16.dp)
            .clip(KudosTheme.shapes.card)
            .then(dndModifier)
            .background(bgColor)
            .border(1.dp, borderColor, KudosTheme.shapes.card)
            .padding(12.dp),
    ) {
        ColumnHeader(status = column.status, count = column.tasks.size)
        Spacer(Modifier.height(12.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 88.dp), // clear the floating glass nav bar
        ) {
            items(
                items = column.tasks,
                key = { it.id },
            ) { task ->
                cardContent(task)
            }
        }
    }
}

@Composable
private fun ColumnHeader(status: TaskStatus, count: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Drawn Moon (not status.text emoji) so the phase glyph renders on iOS too — raw moon emoji
        // show as tofu on iOS/skiko.
        Moon(k = status.fraction, size = 16.dp)
        Text(
            text = status.koLabel(),
            style = KudosTheme.typography.eyebrow,
            color = KudosTheme.colors.ink.ink2,
            modifier = Modifier.weight(1f),
        )
        Box(
            modifier = Modifier
                .clip(KudosTheme.shapes.pill)
                .background(KudosTheme.colors.surface.surface)
                .widthIn(min = 20.dp)
                .height(20.dp)
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = count.toString(),
                style = KudosTheme.typography.identifier,
                color = KudosTheme.colors.ink.ink2,
            )
        }
    }
}

private fun TaskStatus.koLabel(): String = when (this) {
    TaskStatus.BACKLOG -> "백로그"
    TaskStatus.TODO -> "할 일"
    TaskStatus.IN_PROGRESS -> "진행 중"
    TaskStatus.DONE -> "완료"
}

@Preview
@Composable
private fun KanbanBoardPreview() {
    KudosTheme {
        val columns = listOf(
            KanbanColumn(
                TaskStatus.BACKLOG,
                persistentListOf(
                    Task.fixture.copy(id = "b1", title = "Backlog task", status = TaskStatus.BACKLOG),
                ),
            ),
            KanbanColumn(
                TaskStatus.TODO,
                persistentListOf(
                    Task.fixture.copy(id = "t1", title = "Todo task", status = TaskStatus.TODO),
                ),
            ),
            KanbanColumn(TaskStatus.IN_PROGRESS, persistentListOf()),
            KanbanColumn(TaskStatus.DONE, persistentListOf()),
        ).toImmutableList()
        KanbanBoard(
            uiState = KanbanUiState(columns = columns),
            eventFlow = rememberEventFlow(),
        )
    }
}
