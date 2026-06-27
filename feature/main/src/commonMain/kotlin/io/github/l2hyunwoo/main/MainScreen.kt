package io.github.l2hyunwoo.main

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import io.github.l2hyunwoo.core.design.token.LunarDurationStandard
import io.github.l2hyunwoo.core.design.token.LunarStandardEasing
import io.github.l2hyunwoo.kudos.core.common.compose.rememberEventFlow
import io.github.l2hyunwoo.tasks.TaskListEntryPoint
import io.github.l2hyunwoo.tasks.TaskListEvent
import io.github.l2hyunwoo.tasks.TasksContext
import io.github.l2hyunwoo.tasks.component.CreateTaskBottomSheet
import io.github.l2hyunwoo.tasks.rememberTasksContextRetained
import kotlinx.collections.immutable.persistentListOf
import kudos.feature.main.generated.resources.Res
import kudos.feature.main.generated.resources.add_task
import kudos.feature.main.generated.resources.categories
import kudos.feature.main.generated.resources.tasks
import org.jetbrains.compose.resources.stringResource
import soil.query.compose.rememberQuery
import kotlin.time.Clock

enum class MainTab {
    TASKS,
    CATEGORIES
}

@Composable
fun MainScreen(
    tasksContextFactory: TasksContext.Factory,
    categoryContextFactory: CategoryContext.Factory,
    onNavigateToProjectDetail: (String, String, String, String?, String, String) -> Unit = { _, _, _, _, _, _ -> }
) {
    var selectedTab by rememberSaveable { mutableStateOf(MainTab.TASKS) }
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

    // Backdrop recorder hoisted at the screen root; the content container records via Modifier.sky,
    // the floating nav bar (a descendant) blurs it through glassSurface.
    val sky = rememberSky()

    val onAddCurrentTab: () -> Unit = remember(selectedTab) {
        {
            when (selectedTab) {
                MainTab.TASKS -> showCreateTaskSheet = true
                MainTab.CATEGORIES -> showCreateCategorySheet = true
            }
        }
    }

    Scaffold(
        containerColor = KudosTheme.colors.surface.bg,
    ) { _ ->
        Box(Modifier.fillMaxSize()) {
            // Content container records the backdrop so the floating nav bar can blur it.
            Column(
                Modifier
                    .fillMaxSize()
                    .sky(sky)
            ) {
                TodayHeader(
                    selectedTab = selectedTab,
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it },
                )
                Box(Modifier.fillMaxSize()) {
                    when (selectedTab) {
                        MainTab.TASKS -> {
                            with(tasksContext) {
                                TaskListEntryPoint(
                                    eventFlow = tasksEventFlow,
                                    onAddTask = { showCreateTaskSheet = true },
                                    onNavigateToCategories = { selectedTab = MainTab.CATEGORIES },
                                    searchQuery = searchQuery,
                                )
                            }
                        }

                        MainTab.CATEGORIES -> {
                            with(categoryContext) {
                                CategoryListEntryPoint(
                                    eventFlow = categoriesEventFlow,
                                    searchQuery = searchQuery,
                                    onNavigateToProjectDetail = onNavigateToProjectDetail
                                )
                            }
                        }
                    }
                }
            }

            GlassNavBar(
                selectedTab = selectedTab,
                onSelectTab = { selectedTab = it },
                onAdd = onAddCurrentTab,
                sky = sky,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )
        }
    }

    // Create Task Bottom Sheet
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

    // Create Category Bottom Sheet
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

@Composable
private fun TodayHeader(
    selectedTab: MainTab,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 12.dp)
    ) {
        Text(
            text = "좋은 아침 ☾",
            style = KudosTheme.typography.eyebrow,
            color = KudosTheme.colors.brand.primary500,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(
                if (selectedTab == MainTab.TASKS) Res.string.tasks else Res.string.categories
            ),
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
        // Search filters the task list (TASKS tab); on the categories tab it stays a passive field.
        SearchField(query = searchQuery, onQueryChange = onSearchChange)
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
    Row(
        modifier
            .fillMaxWidth()
            .height(64.dp)
            .glassSurface(sky = sky, shape = PillShape),
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
            icon = Icons.Rounded.GridView,
            label = stringResource(Res.string.categories),
            selected = selectedTab == MainTab.CATEGORIES,
            onClick = { onSelectTab(MainTab.CATEGORIES) },
            modifier = Modifier.weight(1f),
        )
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
    Row(
        modifier
            .clip(PillShape)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick)
            .background(pillBg)
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
        if (selected) {
            Spacer(Modifier.width(8.dp))
            Text(text = label, style = KudosTheme.typography.labelLargeM, color = content)
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
            .clip(PillShape)
            .background(KudosTheme.colors.brand.primary600)
            .clickable(interactionSource = interaction, indication = null, onClick = onClick),
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
        Column(Modifier.fillMaxSize().sky(sky)) {
            TodayHeader(selectedTab = tab, searchQuery = "", onSearchChange = {})
        }
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
