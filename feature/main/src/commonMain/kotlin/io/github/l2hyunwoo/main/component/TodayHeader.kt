package io.github.l2hyunwoo.main.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.ViewKanban
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.skydoves.cloudy.Sky
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.core.design.component.surface.glassSurface
import io.github.l2hyunwoo.main.MainTab
import io.github.l2hyunwoo.main.TasksViewMode
import io.github.l2hyunwoo.main.greetingLabel
import io.github.l2hyunwoo.main.todayLabel
import kudos.feature.main.generated.resources.Res
import kudos.feature.main.generated.resources.categories
import kudos.feature.main.generated.resources.dashboard
import kudos.feature.main.generated.resources.tasks
import org.jetbrains.compose.resources.stringResource

// Frosted glass header pinned to the top of the screen. Carries `glassSurface` on its root so the
// full-screen list scrolling behind it is blurred; its own text/search are CHILDREN, which is safe
// because the whole header is hoisted OUTSIDE the `Modifier.sky` recorder by the caller (so they are
// drawn straight to the window, never folded into the blur source — no white band). The glass fill
// extends edge-to-edge, including under the status bar, with the inset applied to the inner content
// so the text never sits under the system clock.
@Composable
fun TodayHeader(
    selectedTab: MainTab,
    showCategories: Boolean,
    tasksViewMode: TasksViewMode,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    darkTheme: Boolean,
    onToggleTheme: (Offset) -> Unit,
    onToggleViewMode: () -> Unit,
    onOpenCategories: () -> Unit,
    onCloseCategories: () -> Unit,
    sky: Sky,
    onHeightChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    // remembered so a keystroke recomposing this header doesn't re-run the epoch arithmetic.
    val greeting = remember { greetingLabel() }
    val today = remember { todayLabel() }
    Column(
        modifier
            .fillMaxWidth()
            .onSizeChanged { onHeightChanged(it.height) }
            // Meets the screen top edge, so only the bottom corners are rounded.
            .glassSurface(sky = sky, shape = HeaderShape)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$greeting ☾",
                style = KudosTheme.typography.eyebrow,
                color = KudosTheme.colors.brand.primary500,
                modifier = Modifier.weight(1f),
            )
            // Header actions. In the Categories overlay only a back affordance shows; otherwise the
            // Tasks tab carries the list/board view toggle, every surface carries a Categories shortcut
            // (Categories was demoted from a nav tab), then the theme toggle.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (showCategories) {
                    HeaderIconButton(
                        icon = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "뒤로",
                        onClick = onCloseCategories,
                    )
                } else {
                    if (selectedTab == MainTab.TASKS) {
                        val isBoard = tasksViewMode == TasksViewMode.BOARD
                        HeaderIconButton(
                            // Icon shows the view the tap switches TO.
                            icon = if (isBoard) {
                                Icons.AutoMirrored.Rounded.ListAlt
                            } else {
                                Icons.Rounded.ViewKanban
                            },
                            contentDescription = if (isBoard) "리스트 보기" else "보드 보기",
                            onClick = onToggleViewMode,
                        )
                    }
                    HeaderIconButton(
                        icon = Icons.Rounded.GridView,
                        contentDescription = stringResource(Res.string.categories),
                        onClick = onOpenCategories,
                    )
                }
                ThemeToggleButton(darkTheme = darkTheme, onToggle = onToggleTheme)
            }
        }
        Spacer(Modifier.height(6.dp))
        val titleRes = when {
            showCategories -> Res.string.categories
            selectedTab == MainTab.DASHBOARD -> Res.string.dashboard
            else -> Res.string.tasks
        }
        Text(
            text = stringResource(titleRes),
            style = KudosTheme.typography.titleLargeB,
            color = KudosTheme.colors.ink.ink,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = today,
            style = KudosTheme.typography.labelMediumR,
            color = KudosTheme.colors.ink.ink2,
        )
        Spacer(Modifier.height(16.dp))
        // Search filters the task list/board and categories; on the dashboard it stays a passive field.
        SearchField(query = searchQuery, onQueryChange = onSearchChange)
    }
}
