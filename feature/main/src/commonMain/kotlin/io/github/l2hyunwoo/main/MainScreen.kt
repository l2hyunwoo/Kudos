package io.github.l2hyunwoo.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.github.l2hyunwoo.category.CategoryContext
import io.github.l2hyunwoo.category.CategoryListEntryPoint
import io.github.l2hyunwoo.category.component.CreateCategoryBottomSheet
import io.github.l2hyunwoo.category.rememberCategoryContextRetained
import io.github.l2hyunwoo.kudos.core.common.compose.rememberEventFlow
import io.github.l2hyunwoo.tasks.TaskListEntryPoint
import io.github.l2hyunwoo.tasks.TaskListEvent
import io.github.l2hyunwoo.tasks.TasksContext
import io.github.l2hyunwoo.tasks.component.CreateTaskBottomSheet
import io.github.l2hyunwoo.tasks.rememberTasksContextRetained
import kudos.feature.main.generated.resources.Res
import kudos.feature.main.generated.resources.add_category
import kudos.feature.main.generated.resources.add_task
import kudos.feature.main.generated.resources.categories
import kudos.feature.main.generated.resources.tasks
import org.jetbrains.compose.resources.stringResource
import soil.query.compose.rememberQuery

enum class MainTab {
    TASKS,
    CATEGORIES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    tasksContextFactory: TasksContext.Factory,
    categoryContextFactory: CategoryContext.Factory,
    onNavigateToProjectDetail: (String, String, String, String?, String, String) -> Unit = { _, _, _, _, _, _ -> }
) {
    var selectedTab by remember { mutableStateOf(MainTab.TASKS) }
    var showCreateTaskSheet by remember { mutableStateOf(false) }
    var showCreateCategorySheet by remember { mutableStateOf(false) }

    val tasksContext = with(tasksContextFactory) {
        rememberTasksContextRetained()
    }
    val categoryContext = with(categoryContextFactory) {
        rememberCategoryContextRetained()
    }

    val tasksEventFlow = rememberEventFlow<TaskListEvent>()

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == MainTab.TASKS,
                    onClick = { selectedTab = MainTab.TASKS },
                    icon = { Icon(Icons.Default.Task, contentDescription = null) },
                    label = { Text(stringResource(Res.string.tasks)) }
                )
                NavigationBarItem(
                    selected = selectedTab == MainTab.CATEGORIES,
                    onClick = { selectedTab = MainTab.CATEGORIES },
                    icon = { Icon(Icons.Default.Category, contentDescription = null) },
                    label = { Text(stringResource(Res.string.categories)) }
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    when (selectedTab) {
                        MainTab.TASKS -> showCreateTaskSheet = true
                        MainTab.CATEGORIES -> showCreateCategorySheet = true
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = when (selectedTab) {
                        MainTab.TASKS -> stringResource(Res.string.add_task)
                        MainTab.CATEGORIES -> stringResource(Res.string.add_category)
                    }
                )
            }
        }
    ) { paddingValues ->
        when (selectedTab) {
            MainTab.TASKS -> {
                with(tasksContext) {
                    TaskListEntryPoint(
                        eventFlow = tasksEventFlow,
                        onNavigateToCategories = { selectedTab = MainTab.CATEGORIES }
                    )
                }
            }

            MainTab.CATEGORIES -> {
                with(categoryContext) {
                    CategoryListEntryPoint(
                        onNavigateToProjectDetail = onNavigateToProjectDetail
                    )
                }
            }
        }
    }

    // Create Task Bottom Sheet
    if (showCreateTaskSheet) {
        with(categoryContext) {
            val categoriesQuery = rememberQuery(categoriesQuery)
            val categories = categoriesQuery.data ?: emptyList()

            CreateTaskBottomSheet(
                categories = kotlinx.collections.immutable.persistentListOf(*categories.toTypedArray()),
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
                    // Create category via CategoryContext
                    showCreateCategorySheet = false
                }
            )
        }
    }
}
