package io.github.l2hyunwoo.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.skydoves.cloudy.rememberSky
import com.skydoves.cloudy.sky
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.main.MainTab
import io.github.l2hyunwoo.main.TasksViewMode

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
            .background(KudosTheme.colors.surface.bg),
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
            onHeightChange = {},
            modifier = Modifier.align(Alignment.TopCenter),
        )
        GlassNavBar(
            selectedTab = tab,
            onSelectTab = { tab = it },
            onAdd = {},
            sky = sky,
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
        )
    }
}
