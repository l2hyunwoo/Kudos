package io.github.l2hyunwoo.tasks.dashboard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.core.design.component.moon.Moon
import io.github.l2hyunwoo.core.design.component.moon.MoonProgress
import io.github.l2hyunwoo.data.tasks.model.TaskPriority
import io.github.l2hyunwoo.data.tasks.model.TaskStatus
import kotlin.math.roundToInt

// Mirrors MainScreen's GlassNavBar footprint (height + vertical margin) so the scroll content can
// reserve room to clear the floating bar at the bottom.
private val NavBarHeight = 64.dp
private val NavBarVerticalMargin = 16.dp

// Read-only summary screen rendered by the Tasks feature. Embedded-style: no chrome of its own —
// MainScreen owns the glass header/nav, so this only reserves [topContentPadding] up top and scrolls
// the rest under the translucent header (same contract as TaskListScreen).
@Composable
fun DashboardScreen(
    uiState: DashboardUiState,
    modifier: Modifier = Modifier,
    topContentPadding: Dp = 0.dp,
) {
    Box(modifier = modifier.fillMaxSize()) {
        if (uiState.totalCount == 0) {
            EmptyDashboard(topContentPadding = topContentPadding)
        } else {
            // The floating glass nav bar (64dp + 16dp vertical margin each side, over the system nav
            // inset) is a MainScreen sibling drawn ON TOP of this scroll area, so reserve its full
            // footprint at the bottom — otherwise the last card scrolls under it and can't be reached.
            val navBarClearance = NavBarHeight + NavBarVerticalMargin * 2 +
                WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Spacer(Modifier.height(topContentPadding))
                CompletionCard(uiState)
                StatusDistributionCard(uiState.statusCounts)
                PriorityDistributionCard(uiState.priorityCounts)
                Spacer(Modifier.height(navBarClearance))
            }
        }
    }
}

// A plain Lunar card matching the design system (surface fill + card shape + soft padding). Used as
// the container for each widget so they read as the same material family as the rest of the app.
@Composable
private fun DashboardCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(KudosTheme.shapes.card)
            .background(KudosTheme.colors.surface.surface)
            .padding(20.dp),
        content = content,
    )
}

@Composable
private fun CompletionCard(uiState: DashboardUiState) {
    DashboardCard {
        Text(
            text = "완료율",
            style = KudosTheme.typography.eyebrow,
            color = KudosTheme.colors.ink.ink3,
        )
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            MoonProgress(
                done = uiState.doneCount,
                total = uiState.totalCount,
                size = 120.dp,
            )
            Spacer(Modifier.width(24.dp))
            Column {
                val percent = (uiState.completionRatio * 100f).roundToInt()
                Text(
                    text = "$percent% 완료",
                    style = KudosTheme.typography.titleLargeB,
                    color = KudosTheme.colors.ink.ink,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${uiState.doneCount} / ${uiState.totalCount}개 할 일",
                    style = KudosTheme.typography.bodyMediumR,
                    color = KudosTheme.colors.ink.ink2,
                )
            }
        }
    }
}

@Composable
private fun StatusDistributionCard(statusCounts: Map<TaskStatus, Int>) {
    // Fixed column order (kanban order) so the bars stay positionally stable across data changes.
    val ordered = StatusOrder.map { it to (statusCounts[it] ?: 0) }
    val maxCount = ordered.maxOf { it.second }.coerceAtLeast(1)
    DashboardCard {
        Text(
            text = "상태 분포",
            style = KudosTheme.typography.eyebrow,
            color = KudosTheme.colors.ink.ink3,
        )
        Spacer(Modifier.height(16.dp))
        ordered.forEachIndexed { index, (status, count) ->
            // Stable key by enum: the row identity never reorders, only the fraction animates.
            StatBar(
                key = status,
                leading = status.text,
                label = status.koLabel(),
                count = count,
                fraction = count.toFloat() / maxCount.toFloat(),
                color = KudosTheme.colors.brand.primary500,
            )
            if (index < ordered.lastIndex) Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun PriorityDistributionCard(priorityCounts: Map<TaskPriority, Int>) {
    val ordered = PriorityOrder.map { it to (priorityCounts[it] ?: 0) }
    val maxCount = ordered.maxOf { it.second }.coerceAtLeast(1)
    DashboardCard {
        Text(
            text = "우선순위 분포",
            style = KudosTheme.typography.eyebrow,
            color = KudosTheme.colors.ink.ink3,
        )
        Spacer(Modifier.height(16.dp))
        ordered.forEachIndexed { index, (priority, count) ->
            StatBar(
                key = priority,
                leading = null,
                label = priority.koLabel(),
                count = count,
                fraction = count.toFloat() / maxCount.toFloat(),
                color = priority.barColor(),
            )
            if (index < ordered.lastIndex) Spacer(Modifier.height(12.dp))
        }
    }
}

// One horizontal bar: optional leading glyph + label, a track with a colored fill scaled to
// [fraction], and a trailing count. The fill width animates with the standard motion token. [key] is
// stable (enum) so the animation tracks a consistent identity.
@Composable
private fun StatBar(
    key: Any,
    leading: String?,
    label: String,
    count: Int,
    fraction: Float,
    color: Color,
) {
    val animatedFraction by animateFloatAsState(
        targetValue = fraction.coerceIn(0f, 1f),
        animationSpec = KudosTheme.motion.standard,
        label = "statBar_$key",
    )
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (leading != null) {
                Text(text = leading, style = KudosTheme.typography.bodyMediumR)
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = label,
                style = KudosTheme.typography.labelLargeM,
                color = KudosTheme.colors.ink.ink2,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = count.toString(),
                style = KudosTheme.typography.identifier,
                color = KudosTheme.colors.ink.ink,
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(KudosTheme.shapes.pill)
                .background(KudosTheme.colors.surface.surface2),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFraction)
                    .height(8.dp)
                    .clip(KudosTheme.shapes.pill)
                    .background(color),
            )
        }
    }
}

@Composable
private fun EmptyDashboard(topContentPadding: Dp) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = topContentPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // New moon: nothing to summarize yet.
        Moon(k = 0f, size = 56.dp, modifier = Modifier.alpha(0.7f))
        Spacer(Modifier.height(16.dp))
        Text(
            text = "아직 할 일이 없어요",
            style = KudosTheme.typography.bodyLargeXB,
            color = KudosTheme.colors.ink.ink2,
            textAlign = TextAlign.Center,
        )
    }
}

private val StatusOrder = listOf(
    TaskStatus.BACKLOG,
    TaskStatus.TODO,
    TaskStatus.IN_PROGRESS,
    TaskStatus.DONE,
)

private val PriorityOrder = listOf(
    TaskPriority.URGENT,
    TaskPriority.HIGH,
    TaskPriority.MEDIUM,
    TaskPriority.LOW,
)

private fun TaskStatus.koLabel(): String = when (this) {
    TaskStatus.BACKLOG -> "백로그"
    TaskStatus.TODO -> "할 일"
    TaskStatus.IN_PROGRESS -> "진행 중"
    TaskStatus.DONE -> "완료"
}

private fun TaskPriority.koLabel(): String = when (this) {
    TaskPriority.URGENT -> "긴급"
    TaskPriority.HIGH -> "높음"
    TaskPriority.MEDIUM -> "보통"
    TaskPriority.LOW -> "낮음"
}

@Composable
private fun TaskPriority.barColor(): Color {
    val p = KudosTheme.colors.priority
    return when (this) {
        TaskPriority.URGENT -> p.urgent
        TaskPriority.HIGH -> p.high
        TaskPriority.MEDIUM -> p.medium
        TaskPriority.LOW -> p.low
    }
}

@Preview
@Composable
private fun DashboardScreenPreview() {
    KudosTheme {
        DashboardScreen(
            uiState = DashboardUiState(
                completionRatio = 0.4f,
                statusCounts = kotlinx.collections.immutable.persistentMapOf(
                    TaskStatus.BACKLOG to 2,
                    TaskStatus.TODO to 4,
                    TaskStatus.IN_PROGRESS to 1,
                    TaskStatus.DONE to 3,
                ),
                priorityCounts = kotlinx.collections.immutable.persistentMapOf(
                    TaskPriority.URGENT to 1,
                    TaskPriority.HIGH to 3,
                    TaskPriority.MEDIUM to 4,
                    TaskPriority.LOW to 2,
                ),
                totalCount = 10,
            ),
        )
    }
}

@Preview
@Composable
private fun DashboardScreenEmptyPreview() {
    KudosTheme {
        DashboardScreen(uiState = aggregate(emptyList()))
    }
}
