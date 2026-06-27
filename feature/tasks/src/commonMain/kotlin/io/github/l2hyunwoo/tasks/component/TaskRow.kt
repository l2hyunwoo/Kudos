package io.github.l2hyunwoo.tasks.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.core.design.component.moon.MoonToggle
import io.github.l2hyunwoo.tasks.formatDueLabel
import io.github.l2hyunwoo.tasks.isOverdue
import io.github.l2hyunwoo.tasks.todayIso
import io.github.l2hyunwoo.data.tasks.model.Task
import io.github.l2hyunwoo.data.tasks.model.TaskPriority
import io.github.l2hyunwoo.data.tasks.model.TaskStatus
import io.github.l2hyunwoo.data.tasks.model.fixture

// The list atom: priority bar · moon toggle · title + meta · chevron. Swipe-left reveals done/delete.
// Visual-only redesign — interaction callbacks are hoisted and default to no-ops so the screen can
// opt in without a presenter/event change.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskRow(
    task: Task,
    modifier: Modifier = Modifier,
    searchQuery: String = "",
    onClick: () -> Unit = {},
    onAdvanceStatus: () -> Unit = {},
    onPickPhase: () -> Unit = {},
    onMarkDone: () -> Unit = {},
    onDelete: () -> Unit = {},
) {
    val dismissState = rememberSwipeToDismissBoxState()

    SwipeToDismissBox(
        state = dismissState,
        onDismiss = { value ->
            when (value) {
                SwipeToDismissBoxValue.EndToStart -> onDelete()
                SwipeToDismissBoxValue.StartToEnd -> onMarkDone()
                SwipeToDismissBoxValue.Settled -> Unit
            }
        },
        backgroundContent = { SwipeBackground(dismissState.targetValue) },
        modifier = modifier,
    ) {
        TaskRowContent(
            task = task,
            searchQuery = searchQuery,
            onClick = onClick,
            onAdvanceStatus = onAdvanceStatus,
            onPickPhase = onPickPhase,
        )
    }
}

@Composable
private fun TaskRowContent(
    task: Task,
    searchQuery: String,
    onClick: () -> Unit,
    onAdvanceStatus: () -> Unit,
    onPickPhase: () -> Unit,
) {
    val isDone = task.status == TaskStatus.DONE
    // Resolve the periwinkle span color outside remember (it's a theme/CompositionLocal read), then
    // key the AnnotatedString on (title, query, color) so it isn't rebuilt on every scroll frame.
    val highlightColor = KudosTheme.colors.brand.primary600
    val title = remember(task.title, searchQuery, highlightColor) {
        highlightedTitle(task.title, searchQuery, highlightColor)
    }
    // Subtle press-scale on the solid card (a ripple would read as heavy on a colored surface). The
    // scale is read inside graphicsLayer (draw phase), so press transitions skip composition/layout.
    // micro is a Float spec, so reduce-motion collapses it automatically.
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        animationSpec = KudosTheme.motion.micro,
        label = "taskRowPressScale",
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(KudosTheme.colors.surface.surface, KudosTheme.shapes.row)
            .clip(KudosTheme.shapes.row)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            // Done rows recede; the moon stays full to read the completion at a glance.
            .alpha(if (isDone) 0.6f else 1f),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PriorityBar(task.priority)
        Spacer(Modifier.width(12.dp))

        MoonToggle(
            k = task.status.fraction,
            onTap = onAdvanceStatus,
            onLongPress = onPickPhase,
            size = 28.dp,
        )
        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = KudosTheme.typography.rowTitle,
                // Base ink color for unmatched text; the highlight span overrides only the matched range.
                color = KudosTheme.colors.ink.ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                // Strikethrough (done) applies to the whole Text, on top of any highlight spans.
                textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None,
            )
            Spacer(Modifier.height(2.dp))
            TaskMeta(task)
        }

        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = KudosTheme.colors.ink.ink3,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(12.dp))
    }
}

// Tints every case-insensitive occurrence of [query] inside [title] with [highlightColor].
// Returns a plain AnnotatedString when there's nothing to highlight, so the no-search path renders
// byte-identically to the old plain-String Text. Pure (no Composable/theme reads) so the caller can
// remember it keyed on its inputs.
private fun highlightedTitle(
    title: String,
    query: String,
    highlightColor: Color,
): AnnotatedString {
    val needle = query.trim()
    // Empty/blank query or a query longer than the title can never match: emit the plain string.
    if (needle.isEmpty() || needle.length > title.length) return AnnotatedString(title)

    return buildAnnotatedString {
        var cursor = 0
        while (cursor <= title.length - needle.length) {
            val match = title.indexOf(needle, cursor, ignoreCase = true)
            if (match < 0) break
            // Unmatched gap before this occurrence, then the tinted match.
            append(title.substring(cursor, match))
            withStyle(SpanStyle(color = highlightColor)) {
                append(title.substring(match, match + needle.length))
            }
            // Advance by the full match length so overlapping starts aren't double-counted.
            cursor = match + needle.length
        }
        // Tail after the last match (or the whole string if the loop never matched).
        append(title.substring(cursor))
    }
}

// id (tabular identifier) · project chip (pastel) · due (caption). Tags axis is not in the data
// model yet, so the project title doubles as the single chip for now.
@Composable
private fun TaskMeta(task: Task) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = task.taskId.uppercase(),
            style = KudosTheme.typography.identifier,
            color = KudosTheme.colors.ink.ink3,
            maxLines = 1,
        )
        task.projectTitle?.let { project ->
            TagChip(label = project, dot = KudosTheme.colors.pastels.lilac)
        }
        task.dueDate?.let { due ->
            // todayIso() is a cheap epoch-day computation; recomputing per row keeps the helper pure
            // and avoids threading "today" through the row signature. Overdue labels tint red to match
            // the Lunar spec.
            val today = todayIso()
            val overdue = isOverdue(due, today)
            Text(
                text = formatDueLabel(due, today),
                style = KudosTheme.typography.labelLargeM,
                color = if (overdue) KudosTheme.colors.priority.urgent else KudosTheme.colors.ink.ink2,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun TagChip(label: String, dot: Color) {
    val (bg, fg) = KudosTheme.colors.pastelChip(dot)
    Text(
        text = label,
        style = KudosTheme.typography.labelLargeM,
        color = fg,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clip(KudosTheme.shapes.chipSmall)
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

// Small left accent bar + dot — priority is felt, never shouted (must not fight the moon).
@Composable
private fun PriorityBar(priority: TaskPriority) {
    val color = priority.toColor()
    Box(
        modifier = Modifier
            .padding(start = 4.dp)
            .width(4.dp)
            .fillMaxHeight()
            .padding(vertical = 14.dp)
            .clip(KudosTheme.shapes.pill)
            .background(color),
    )
}

@Composable
private fun TaskPriority.toColor(): Color {
    val p = KudosTheme.colors.priority
    return when (this) {
        TaskPriority.URGENT -> p.urgent
        TaskPriority.HIGH -> p.high
        TaskPriority.MEDIUM -> p.medium
        TaskPriority.LOW -> p.low
    }
}

@Composable
private fun SwipeBackground(target: SwipeToDismissBoxValue) {
    val isDelete = target == SwipeToDismissBoxValue.EndToStart
    val color = if (isDelete) KudosTheme.colors.priority.urgent else KudosTheme.colors.priority.low
    val alignment = if (isDelete) Alignment.CenterEnd else Alignment.CenterStart
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(KudosTheme.shapes.row)
            .background(color)
            .padding(horizontal = 24.dp),
        contentAlignment = alignment,
    ) {
        Icon(
            imageVector = if (isDelete) Icons.Default.Delete else Icons.Default.Check,
            contentDescription = null,
            tint = Color.White,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskRowPreview() {
    KudosTheme {
        TaskRow(task = Task.fixture)
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskRowDonePreview() {
    KudosTheme {
        TaskRow(task = Task.fixture.copy(status = TaskStatus.DONE))
    }
}

// fixture title is "Sample Task" → "amp" (in "Sample") is tinted periwinkle.
@Preview(showBackground = true)
@Composable
private fun TaskRowHighlightPreview() {
    KudosTheme {
        TaskRow(task = Task.fixture, searchQuery = "amp")
    }
}
