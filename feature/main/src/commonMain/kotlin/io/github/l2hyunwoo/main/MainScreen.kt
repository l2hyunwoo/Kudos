package io.github.l2hyunwoo.main

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.skydoves.cloudy.rememberSky
import com.skydoves.cloudy.sky
import io.github.l2hyunwoo.category.CategoryContext
import io.github.l2hyunwoo.category.CategoryListEntryPoint
import io.github.l2hyunwoo.category.CategoryListEvent
import io.github.l2hyunwoo.category.component.CreateCategoryBottomSheet
import io.github.l2hyunwoo.category.rememberCategoryContextRetained
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.core.design.token.LunarDurationMoonFill
import io.github.l2hyunwoo.core.design.token.LunarDurationStandard
import io.github.l2hyunwoo.kudos.core.common.compose.rememberEventFlow
import io.github.l2hyunwoo.main.component.GlassNavBar
import io.github.l2hyunwoo.main.component.TodayHeader
import io.github.l2hyunwoo.data.tasks.model.Task
import io.github.l2hyunwoo.tasks.TaskListEntryPoint
import io.github.l2hyunwoo.tasks.TaskListEvent
import io.github.l2hyunwoo.tasks.TasksContext
import io.github.l2hyunwoo.tasks.board.KanbanBoardEntryPoint
import io.github.l2hyunwoo.tasks.component.CreateTaskBottomSheet
import io.github.l2hyunwoo.tasks.dashboard.DashboardEntryPoint
import io.github.l2hyunwoo.tasks.rememberTasksContextRetained
import kotlinx.collections.immutable.persistentListOf
import soil.query.compose.rememberQuery

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    tasksContextFactory: TasksContext.Factory,
    categoryContextFactory: CategoryContext.Factory,
    darkTheme: Boolean = false,
    onToggleTheme: (Offset) -> Unit = {},
    onNavigateToTaskDetail: (Task) -> Unit = {},
    onNavigateToProjectDetail: (String, String, String, String?, String, String) -> Unit = { _, _, _, _, _, _ -> }
) {
    val chrome = rememberMainChromeState()
    val sheet = rememberSheetState()
    // Kept standalone (not in chrome) so a per-keystroke write doesn't invalidate selection readers.
    var searchQuery by rememberSaveable { mutableStateOf("") }

    val tasksContext = with(tasksContextFactory) {
        rememberTasksContextRetained()
    }
    val categoryContext = with(categoryContextFactory) {
        rememberCategoryContextRetained()
    }

    val tasksEventFlow = rememberEventFlow<TaskListEvent>()
    val categoriesEventFlow = rememberEventFlow<CategoryListEvent>()

    // Single backdrop recorder for the screen. The swappable tab content records via Modifier.sky;
    // the glass chrome (header, nav bar) sits OUTSIDE that recorder and blurs it via glassSurface.
    val sky = rememberSky()

    // Measured glass-header height: the list reserves it as top contentPadding so its first row starts
    // below the header while the rest scrolls UNDER the translucent header (what makes the frosting
    // visible). Variable height (inset + greeting + title + date + search), so measured not hard-coded.
    var headerHeightPixels by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val headerHeight = with(density) { headerHeightPixels.toDp() }

    // Owned here so the indicator can be a sibling of the recorder (crisp over the glass); the Tasks
    // list attaches the gesture and reports state back via tasksRefreshing.
    val pullToRefreshState = rememberPullToRefreshState()
    var tasksRefreshing by remember { mutableStateOf(false) }

    // Keyed on field reads, not on chrome — the holder instance is stable across recompositions, so
    // keying on it would freeze the lambda.
    val onAddCurrentTab: () -> Unit = remember(chrome.selectedTab, chrome.showCategories) {
        {
            when {
                chrome.showCategories -> sheet.showCreateCategory()
                else -> sheet.showCreateTask()
            }
        }
    }

    // Re-capture the backdrop whenever the recorded content swaps under the glass: a tab change, a
    // list/board toggle, opening/closing the Categories overlay, or a theme flip. A Crossfade/content
    // swap or color flip is not a scroll, so the recorder's scroll-driven re-arm never fires for it;
    // without this the glass chrome keeps blurring the previous content/theme (ghost). Pass the
    // longest of the dissolve / theme-reveal durations so the blur tracks the whole transition instead
    // of freezing partway once a short settle tail elapses.
    LaunchedEffect(chrome.selectedTab, chrome.tasksViewMode, chrome.showCategories, darkTheme) {
        sky.invalidate(maxOf(LunarDurationStandard, LunarDurationMoonFill).toLong())
    }

    Scaffold(
        containerColor = KudosTheme.colors.surface.bg,
    ) { _ ->
        Box(Modifier.fillMaxSize()) {
            // Full-screen backdrop recorder: the swappable tab subtree is the ONLY thing recorded
            // into the blur source (Modifier.sky). The list spans the whole screen and scrolls UNDER
            // the glass header/nav bar, which are layered OUTSIDE this recorder so their own
            // foreground never pollutes the blur source.
            Box(
                Modifier
                    .fillMaxSize()
                    .sky(sky)
            ) {
                // Cross-fade the recorded subtree instead of hard-swapping it (the reported tab-switch
                // flicker). The target encodes every distinct surface — the Categories overlay, the
                // active tab, and (on Tasks) the list/board view — so each swap dissolves. standard is
                // a Float spec, so reduce-motion collapses it for free.
                val contentKey: Any = when {
                    chrome.showCategories -> "categories"
                    chrome.selectedTab == MainTab.DASHBOARD -> "dashboard"
                    else -> chrome.tasksViewMode
                }
                Crossfade(
                    targetState = contentKey,
                    animationSpec = KudosTheme.motion.standard,
                    label = "tabContent",
                ) { key ->
                    when (key) {
                        "categories" -> {
                            with(categoryContext) {
                                CategoryListEntryPoint(
                                    eventFlow = categoriesEventFlow,
                                    searchQuery = searchQuery,
                                    topContentPadding = headerHeight,
                                    onNavigateToProjectDetail = onNavigateToProjectDetail
                                )
                            }
                        }

                        "dashboard" -> {
                            with(tasksContext) {
                                DashboardEntryPoint(topContentPadding = headerHeight)
                            }
                        }

                        TasksViewMode.BOARD -> {
                            with(tasksContext) {
                                KanbanBoardEntryPoint(
                                    eventFlow = tasksEventFlow,
                                    onNavigateToTaskDetail = onNavigateToTaskDetail,
                                    searchQuery = searchQuery,
                                    topContentPadding = headerHeight,
                                )
                            }
                        }

                        else -> {
                            with(tasksContext) {
                                TaskListEntryPoint(
                                    eventFlow = tasksEventFlow,
                                    onAddTask = { sheet.showCreateTask() },
                                    onNavigateToTaskDetail = onNavigateToTaskDetail,
                                    searchQuery = searchQuery,
                                    topContentPadding = headerHeight,
                                    // MainScreen owns the glass header/nav/FAB; the fallback must be
                                    // chrome-less so it doesn't bleed through the glass.
                                    embedded = true,
                                    // PTR gesture lives on the list (inside the recorder); the
                                    // indicator is drawn below as a sibling (outside the recorder).
                                    pullToRefreshState = pullToRefreshState,
                                    onRefreshingChanged = { tasksRefreshing = it },
                                )
                            }
                        }
                    }
                }
            }

            // Frosted glass header, pinned to the top and hoisted OUT of the recorder (a sibling of
            // the recorded content), so the list scrolling behind it is blurred with no band. The
            // header's text/search are its own children (drawn to the window, never recorded), so the
            // search field stays crisp and interactive.
            TodayHeader(
                selectedTab = chrome.selectedTab,
                showCategories = chrome.showCategories,
                tasksViewMode = chrome.tasksViewMode,
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                darkTheme = darkTheme,
                onToggleTheme = onToggleTheme,
                onToggleViewMode = chrome::toggleTasksViewMode,
                onOpenCategories = { chrome.showCategories = true },
                onCloseCategories = { chrome.showCategories = false },
                sky = sky,
                onHeightChanged = { headerHeightPixels = it },
                modifier = Modifier.align(Alignment.TopCenter),
            )

            GlassNavBar(
                selectedTab = chrome.selectedTab,
                // Selecting a nav tab always exits the Categories overlay (encapsulated in selectTab).
                onSelectTab = chrome::selectTab,
                onAdd = onAddCurrentTab,
                sky = sky,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )

            // Pull-to-refresh indicator: a sibling of the recorder (never recorded), so it stays crisp
            // over the glass instead of being blurred. Offset below the measured glass header so it
            // sits in the same band as the first list row. Only the Tasks LIST view wires PTR (the
            // board has no vertical pull container, dashboard/categories aren't refreshable here).
            if (chrome.selectedTab == MainTab.TASKS &&
                chrome.tasksViewMode == TasksViewMode.LIST &&
                !chrome.showCategories
            ) {
                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = tasksRefreshing,
                    color = KudosTheme.colors.brand.primary600,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = headerHeight),
                )
            }
        }
    }

    when (sheet.active) {
        ActiveSheet.CREATE_TASK -> {
            with(categoryContext) {
                val categoriesQuery = rememberQuery(categoriesQuery)
                val categoriesWithProjects = categoriesQuery.data ?: emptyList()

                CreateTaskBottomSheet(
                    categories = persistentListOf(*categoriesWithProjects.toTypedArray()),
                    onDismiss = { sheet.dismiss() },
                    onCreate = { request ->
                        tasksEventFlow.tryEmit(TaskListEvent.CreateTask(request))
                        sheet.dismiss()
                    }
                )
            }
        }

        ActiveSheet.CREATE_CATEGORY -> {
            with(categoryContext) {
                CreateCategoryBottomSheet(
                    onDismiss = { sheet.dismiss() },
                    onCreate = { request ->
                        categoriesEventFlow.tryEmit(CategoryListEvent.CreateCategory(request))
                        sheet.dismiss()
                    }
                )
            }
        }

        null -> Unit
    }
}
