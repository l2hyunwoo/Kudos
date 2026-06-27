package io.github.l2hyunwoo.tasks

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// MainScreen's floating GlassNavBar footprint (height + 16dp vertical margin each side, over the
// system nav inset). It is a sibling drawn ON TOP of each tab's full-screen scroll area, so every
// scrollable here must reserve this much bottom space — otherwise the last row scrolls under the bar
// and can't be reached. Mirrors the GlassNavBar dimensions in MainScreen.
private val NavBarHeight = 64.dp
private val NavBarVerticalMargin = 16.dp

@Composable
internal fun rememberNavBarClearance(extra: Dp = 16.dp): Dp {
    val systemBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    return NavBarHeight + NavBarVerticalMargin * 2 + systemBottom + extra
}
