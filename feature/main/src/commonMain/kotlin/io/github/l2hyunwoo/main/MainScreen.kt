package io.github.l2hyunwoo.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ViewKanban
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.skydoves.cloudy.Sky
import com.skydoves.cloudy.rememberSky
import com.skydoves.cloudy.sky
import io.github.l2hyunwoo.category.CategoryContext
import io.github.l2hyunwoo.category.CategoryListEntryPoint
import io.github.l2hyunwoo.category.CategoryListEvent
import io.github.l2hyunwoo.category.component.CreateCategoryBottomSheet
import io.github.l2hyunwoo.category.rememberCategoryContextRetained
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.core.design.component.surface.glassSurface
import io.github.l2hyunwoo.core.design.token.KudosShapes
import io.github.l2hyunwoo.core.design.token.LunarDurationMicro
import io.github.l2hyunwoo.core.design.token.LunarDurationStandard
import io.github.l2hyunwoo.core.design.token.LunarStandardEasing
import io.github.l2hyunwoo.kudos.core.common.compose.rememberEventFlow
import io.github.l2hyunwoo.data.tasks.model.Task
import io.github.l2hyunwoo.tasks.TaskListEntryPoint
import io.github.l2hyunwoo.tasks.TaskListEvent
import io.github.l2hyunwoo.tasks.TasksContext
import io.github.l2hyunwoo.tasks.board.KanbanBoardEntryPoint
import io.github.l2hyunwoo.tasks.component.CreateTaskBottomSheet
import io.github.l2hyunwoo.tasks.dashboard.DashboardEntryPoint
import io.github.l2hyunwoo.tasks.rememberTasksContextRetained
import kotlinx.collections.immutable.persistentListOf
import kudos.feature.main.generated.resources.Res
import kudos.feature.main.generated.resources.add_task
import kudos.feature.main.generated.resources.categories
import kudos.feature.main.generated.resources.dashboard
import kudos.feature.main.generated.resources.tasks
import org.jetbrains.compose.resources.stringResource
import soil.query.compose.rememberQuery
import kotlin.time.Clock

enum class MainTab {
    TASKS,
    DASHBOARD
}

// Tasks-tab view toggle: the same task data rendered as a vertical list or a horizontal kanban board.
enum class TasksViewMode {
    LIST,
    BOARD
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    tasksContextFactory: TasksContext.Factory,
    categoryContextFactory: CategoryContext.Factory,
    darkTheme: Boolean = false,
    onToggleTheme: () -> Unit = {},
    onNavigateToTaskDetail: (Task) -> Unit = {},
    onNavigateToProjectDetail: (String, String, String, String?, String, String) -> Unit = { _, _, _, _, _, _ -> }
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.TASKS) }
    // Tasks-tab list/board toggle, owned here so the board renders inside the same sky recorder.
    var tasksViewMode by rememberSaveable { mutableStateOf(TasksViewMode.LIST) }
    // Categories is no longer a nav tab (option c-1): it is reached from a header action and shown as
    // a full-screen in-place overlay that keeps the glass chrome, so no new nav route is needed.
    var showCategories by rememberSaveable { mutableStateOf(false) }
    var showCreateTaskSheet by remember { mutableStateOf(false) }
    var showCreateCategorySheet by remember { mutableStateOf(false) }
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

    // Measured height of the glass header overlay. The header floats over the full-screen list, so
    // the list reserves this as top contentPadding to start its first row below the header (the rest
    // scrolls UNDER the translucent header, which is what makes the frosting visible). The header
    // height is variable (status-bar inset + greeting + title + date + search), so it is measured
    // rather than hard-coded.
    var headerHeightPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val headerHeight = with(density) { headerHeightPx.toDp() }

    // Pull-to-refresh is owned here so its indicator can be a sibling of the recorder (crisp over the
    // glass). The Tasks tab attaches the gesture to its list and reports validation state back via
    // tasksRefreshing; the indicator below renders with the same state.
    val ptrState = rememberPullToRefreshState()
    var tasksRefreshing by remember { mutableStateOf(false) }

    // The center "+" adds in the active surface: a task on Tasks/Categories, nothing on Dashboard
    // (a read-only summary). Keyed on the surfaces that change its meaning.
    val onAddCurrentTab: () -> Unit = remember(selectedTab, showCategories) {
        {
            when {
                showCategories -> showCreateCategorySheet = true
                selectedTab == MainTab.TASKS -> showCreateTaskSheet = true
                else -> Unit
            }
        }
    }

    // Re-capture the backdrop whenever the recorded content swaps under the glass: a tab change, a
    // list/board toggle, or opening/closing the Categories overlay. A Crossfade/content swap is not a
    // scroll, so the recorder's scroll-driven re-arm never fires for it; without this the glass chrome
    // keeps blurring the previous content (ghost). Pass the cross-fade duration so the blur tracks the
    // whole dissolve instead of freezing partway once a short settle tail elapses.
    LaunchedEffect(selectedTab, tasksViewMode, showCategories) {
        sky.invalidate(LunarDurationStandard.toLong())
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
                    showCategories -> "categories"
                    selectedTab == MainTab.DASHBOARD -> "dashboard"
                    else -> tasksViewMode
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
                                    onAddTask = { showCreateTaskSheet = true },
                                    onNavigateToTaskDetail = onNavigateToTaskDetail,
                                    searchQuery = searchQuery,
                                    topContentPadding = headerHeight,
                                    // MainScreen owns the glass header/nav/FAB; the fallback must be
                                    // chrome-less so it doesn't bleed through the glass.
                                    embedded = true,
                                    // PTR gesture lives on the list (inside the recorder); the
                                    // indicator is drawn below as a sibling (outside the recorder).
                                    pullToRefreshState = ptrState,
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
                selectedTab = selectedTab,
                showCategories = showCategories,
                tasksViewMode = tasksViewMode,
                searchQuery = searchQuery,
                onSearchChange = { searchQuery = it },
                darkTheme = darkTheme,
                onToggleTheme = onToggleTheme,
                onToggleViewMode = {
                    tasksViewMode = if (tasksViewMode == TasksViewMode.LIST) {
                        TasksViewMode.BOARD
                    } else {
                        TasksViewMode.LIST
                    }
                },
                onOpenCategories = { showCategories = true },
                onCloseCategories = { showCategories = false },
                sky = sky,
                onHeightChanged = { headerHeightPx = it },
                modifier = Modifier.align(Alignment.TopCenter),
            )

            GlassNavBar(
                selectedTab = selectedTab,
                onSelectTab = {
                    // Selecting a nav tab always exits the Categories overlay.
                    showCategories = false
                    selectedTab = it
                },
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
            if (selectedTab == MainTab.TASKS && tasksViewMode == TasksViewMode.LIST && !showCategories) {
                PullToRefreshDefaults.Indicator(
                    state = ptrState,
                    isRefreshing = tasksRefreshing,
                    color = KudosTheme.colors.brand.primary600,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = headerHeight),
                )
            }
        }
    }

    if (showCreateTaskSheet) {
        with(categoryContext) {
            val categoriesQuery = rememberQuery(categoriesQuery)
            val categoriesWithProjects = categoriesQuery.data ?: emptyList()

            CreateTaskBottomSheet(
                categories = persistentListOf(*categoriesWithProjects.toTypedArray()),
                onDismiss = { showCreateTaskSheet = false },
                onCreate = { request ->
                    tasksEventFlow.tryEmit(TaskListEvent.CreateTask(request))
                    showCreateTaskSheet = false
                }
            )
        }
    }

    if (showCreateCategorySheet) {
        with(categoryContext) {
            CreateCategoryBottomSheet(
                onDismiss = { showCreateCategorySheet = false },
                onCreate = { request ->
                    categoriesEventFlow.tryEmit(CategoryListEvent.CreateCategory(request))
                    showCreateCategorySheet = false
                }
            )
        }
    }
}

// Frosted glass header pinned to the top of the screen. Carries `glassSurface` on its root so the
// full-screen list scrolling behind it is blurred; its own text/search are CHILDREN, which is safe
// because the whole header is hoisted OUTSIDE the `Modifier.sky` recorder by the caller (so they are
// drawn straight to the window, never folded into the blur source — no white band). The glass fill
// extends edge-to-edge, including under the status bar, with the inset applied to the inner content
// so the text never sits under the system clock.
@Composable
private fun TodayHeader(
    selectedTab: MainTab,
    showCategories: Boolean,
    tasksViewMode: TasksViewMode,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    darkTheme: Boolean,
    onToggleTheme: () -> Unit,
    onToggleViewMode: () -> Unit,
    onOpenCategories: () -> Unit,
    onCloseCategories: () -> Unit,
    sky: Sky,
    onHeightChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
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
                text = "좋은 아침 ☾",
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
            text = todayLabel(),
            style = KudosTheme.typography.labelMediumR,
            color = KudosTheme.colors.ink.ink2,
        )
        Spacer(Modifier.height(16.dp))
        // Search filters the task list/board and categories; on the dashboard it stays a passive field.
        SearchField(query = searchQuery, onQueryChange = onSearchChange)
    }
}

// Round translucent header action button, matching ThemeToggleButton's footprint so the action row
// reads as one cluster.
@Composable
private fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier
            .size(36.dp)
            .clip(PillShape)
            .background(KudosTheme.colors.brand.primary050)
            .clickable(
                interactionSource = interaction,
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = KudosTheme.colors.brand.primary500,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun ThemeToggleButton(
    darkTheme: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier
            .size(36.dp)
            // clip precedes clickable so the bounded ripple is masked to the circle.
            .clip(PillShape)
            .background(KudosTheme.colors.brand.primary050)
            .clickable(
                interactionSource = interaction,
                indication = ripple(bounded = true),
                onClick = onToggle,
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Icon shows the mode the tap switches TO: a sun while dark, a moon while light.
        Icon(
            imageVector = if (darkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
            contentDescription = if (darkTheme) "밝은 테마로 전환" else "어두운 테마로 전환",
            tint = KudosTheme.colors.brand.primary500,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun SearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .clip(PillShape)
            .background(KudosTheme.colors.surface.surface2)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            tint = KudosTheme.colors.ink.ink3,
            modifier = Modifier.size(20.dp),
        )
        Spacer(Modifier.width(10.dp))
        Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (query.isEmpty()) {
                Text(
                    text = "검색",
                    style = KudosTheme.typography.bodyMediumR,
                    color = KudosTheme.colors.ink.ink3,
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = KudosTheme.typography.bodyMediumR.copy(color = KudosTheme.colors.ink.ink),
                cursorBrush = SolidColor(KudosTheme.colors.brand.primary600),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (query.isNotEmpty()) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "검색어 지우기",
                tint = KudosTheme.colors.ink.ink3,
                modifier = Modifier
                    .size(20.dp)
                    .clip(PillShape)
                    .clickable { onQueryChange("") },
            )
        }
    }
}

@Composable
private fun GlassNavBar(
    selectedTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    onAdd: () -> Unit,
    sky: Sky,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxWidth().height(64.dp)) {
        // Background: blurred fill + shadow/clip/border, no children (background-only glassSurface).
        Box(
            Modifier
                .fillMaxSize()
                .glassSurface(sky = sky, shape = PillShape),
        )
        // Foreground sibling: the nav items, overlaid on (not children of) the glass background, so
        // none of their pixels fold back into the blur source.
        Row(
            Modifier
                .fillMaxSize()
                // Inner inset so a selected item's pill never reaches the glass bar's rounded edge;
                // without it the parent pill clip shears the item-pill's outer corner ("squished").
                .padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            NavBarItem(
                icon = Icons.AutoMirrored.Rounded.ListAlt,
                label = stringResource(Res.string.tasks),
                selected = selectedTab == MainTab.TASKS,
                onClick = { onSelectTab(MainTab.TASKS) },
                modifier = Modifier.weight(1f),
            )
            CenterAddButton(
                onClick = onAdd,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            NavBarItem(
                icon = Icons.Rounded.BarChart,
                label = stringResource(Res.string.dashboard),
                selected = selectedTab == MainTab.DASHBOARD,
                onClick = { onSelectTab(MainTab.DASHBOARD) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun NavBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // KudosMotion specs are FiniteAnimationSpec<Float>; color animation needs a Color spec, so
    // rebuild one from the same standard duration + easing tokens.
    val colorSpec = tween<Color>(LunarDurationStandard, easing = LunarStandardEasing)
    val content by animateColorAsState(
        targetValue = if (selected) KudosTheme.colors.brand.primary600 else KudosTheme.colors.ink.ink3,
        animationSpec = colorSpec,
    )
    val pillBg by animateColorAsState(
        targetValue = if (selected) {
            KudosTheme.colors.brand.primary100
        } else {
            Color.Transparent
        },
        animationSpec = colorSpec,
    )
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    // Subtle press feedback: dip the pill on press, settle on release. micro is a Float spec so
    // reduce-motion collapses it automatically.
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = KudosTheme.motion.micro,
        label = "navPillScale",
    )
    // Outer = the weighted slot; an inner horizontal margin keeps the highlighted pill off the
    // slot edge so its rounded corners stay intact. clip precedes clickable so the ripple is
    // bounded to the pill shape, not the raw rectangular slot.
    Box(
        modifier.padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            Modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .clip(PillShape)
                .background(pillBg)
                .clickable(
                    interactionSource = interaction,
                    indication = ripple(bounded = true),
                    onClick = onClick,
                )
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = content,
                modifier = Modifier.size(22.dp),
            )
            // The label expands+fades in on selection instead of snapping, so the pill grows into
            // its selected width. Typed specs rebuilt from the micro duration + standard easing.
            AnimatedVisibility(
                visible = selected,
                enter = expandHorizontally(
                    animationSpec = tween(LunarDurationStandard, easing = LunarStandardEasing),
                ) + fadeIn(animationSpec = tween(LunarDurationStandard, easing = LunarStandardEasing)),
                exit = shrinkHorizontally(
                    animationSpec = tween(LunarDurationMicro, easing = LunarStandardEasing),
                ) + fadeOut(animationSpec = tween(LunarDurationMicro, easing = LunarStandardEasing)),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(Modifier.width(8.dp))
                    Text(text = label, style = KudosTheme.typography.labelLargeM, color = content)
                }
            }
        }
    }
}

@Composable
private fun CenterAddButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    Box(
        modifier
            .size(48.dp)
            // clip precedes clickable so the bounded ripple is masked to the circle.
            .clip(PillShape)
            .background(KudosTheme.colors.brand.primary600)
            .clickable(
                interactionSource = interaction,
                indication = ripple(bounded = true),
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.Add,
            contentDescription = stringResource(Res.string.add_task),
            tint = Color.White,
            modifier = Modifier.size(24.dp),
        )
    }
}

// Full pill: PillRadius (999dp) caps to half the smaller dimension for any chrome height.
private val PillShape = RoundedCornerShape(KudosShapes.PillRadius)

// Glass header outline: meets the screen top edge (square top), soft-rounded bottom so the frosted
// panel reads as a sheet hanging from the top rather than a free-floating card.
private val HeaderShape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)

// Monday-first to match a Korean calendar header.
private val KoreanWeekdays = listOf("월", "화", "수", "목", "금", "토", "일")

// "6월 27일 (금)" — derived from epoch days with the civil-date algorithm so commonMain needs no
// extra datetime dependency. Greeting-only header, so the UTC-based day is acceptable.
private fun todayLabel(): String {
    val epochSeconds = Clock.System.now().epochSeconds
    val epochDays = epochSeconds.floorDiv(86_400L)
    val (month, day) = civilMonthDay(epochDays)
    // 1970-01-01 was a Thursday (index 3 in Mon=0 weekday list).
    val weekday = KoreanWeekdays[(epochDays + 3).mod(7L).toInt()]
    return "${month}월 ${day}일 ($weekday)"
}

// Howard Hinnant's civil_from_days: epoch day count -> (month, day-of-month). Year is unused here.
private fun civilMonthDay(epochDays: Long): Pair<Int, Int> {
    val z = epochDays + 719_468L
    val era = (if (z >= 0) z else z - 146_096L) / 146_097L
    val doe = z - era * 146_097L
    val yoe = (doe - doe / 1460L + doe / 36_524L - doe / 146_096L) / 365L
    val doy = doe - (365L * yoe + yoe / 4L - yoe / 100L)
    val mp = (5L * doy + 2L) / 153L
    val day = (doy - (153L * mp + 2L) / 5L + 1L).toInt()
    val month = (if (mp < 10L) mp + 3L else mp - 9L).toInt()
    return month to day
}

@Preview
@Composable
private fun MainScreenChromePreviewLight() {
    KudosTheme(darkTheme = false) {
        ChromePreviewScaffold()
    }
}

@Preview
@Composable
private fun MainScreenChromePreviewDark() {
    KudosTheme(darkTheme = true) {
        ChromePreviewScaffold()
    }
}

@Composable
private fun ChromePreviewScaffold() {
    val sky = rememberSky()
    var tab by remember { mutableStateOf(MainTab.TASKS) }
    Box(
        Modifier
            .fillMaxSize()
            .background(KudosTheme.colors.surface.bg)
    ) {
        // Full-screen recorder (the list would live here at runtime).
        Box(Modifier.fillMaxSize().sky(sky))
        // Glass header overlay, pinned to the top, outside the recorder.
        TodayHeader(
            selectedTab = tab,
            showCategories = false,
            tasksViewMode = TasksViewMode.LIST,
            searchQuery = "",
            onSearchChange = {},
            darkTheme = false,
            onToggleTheme = {},
            onToggleViewMode = {},
            onOpenCategories = {},
            onCloseCategories = {},
            sky = sky,
            onHeightChanged = {},
            modifier = Modifier.align(Alignment.TopCenter),
        )
        GlassNavBar(
            selectedTab = tab,
            onSelectTab = { tab = it },
            onAdd = {},
            sky = sky,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        )
    }
}
