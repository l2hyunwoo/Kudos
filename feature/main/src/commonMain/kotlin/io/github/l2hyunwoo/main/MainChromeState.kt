package io.github.l2hyunwoo.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

// Holds the screen's nav/view selection: which surface is shown. Per-field mutableStateOf so reading
// one field doesn't subscribe a reader to the others.
@Stable
class MainChromeState(
    selectedTab: MainTab = MainTab.TASKS,
    tasksViewMode: TasksViewMode = TasksViewMode.LIST,
    showCategories: Boolean = false,
) {
    var selectedTab by mutableStateOf(selectedTab)
    var tasksViewMode by mutableStateOf(tasksViewMode)
    var showCategories by mutableStateOf(showCategories)

    // Selecting a nav tab always exits the Categories overlay.
    fun selectTab(tab: MainTab) {
        selectedTab = tab
        showCategories = false
    }

    fun toggleTasksViewMode() {
        tasksViewMode =
            if (tasksViewMode == TasksViewMode.LIST) {
                TasksViewMode.BOARD
            } else {
                TasksViewMode.LIST
            }
    }

    companion object {
        val Saver: Saver<MainChromeState, Any> =
            listSaver(
                save = { listOf(it.selectedTab.name, it.tasksViewMode.name, it.showCategories) },
                restore = {
                    MainChromeState(
                        selectedTab = MainTab.valueOf(it[0] as String),
                        tasksViewMode = TasksViewMode.valueOf(it[1] as String),
                        showCategories = it[2] as Boolean,
                    )
                },
            )
    }
}

@Composable
fun rememberMainChromeState(): MainChromeState = rememberSaveable(saver = MainChromeState.Saver) { MainChromeState() }
