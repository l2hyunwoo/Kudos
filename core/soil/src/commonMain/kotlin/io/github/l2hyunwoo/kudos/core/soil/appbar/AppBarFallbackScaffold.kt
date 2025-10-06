package io.github.l2hyunwoo.kudos.core.soil.appbar

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import io.github.l2hyunwoo.core.design.safeDrawingWithBottomNavBar
import io.github.l2hyunwoo.kudos.core.soil.FallbackContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
context(_: FallbackContext)
fun AppBarFallbackScaffold(
    title: String,
    onBackClick: (() -> Unit)? = null,
    appBarSize: AppBarSize = AppBarSize.DEFAULT,
    appBarColors: TopAppBarColors = TopAppBarDefaults.topAppBarColors().copy(
        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
    ),
    windowInsets: WindowInsets = WindowInsets.safeDrawingWithBottomNavBar,
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable (innerPadding: PaddingValues) -> Unit,
) {
    Scaffold(
        topBar = {
            when (appBarSize) {
                AppBarSize.DEFAULT -> {
                    AnimatedTextTopAppBar(
                        title = title,
                        colors = appBarColors,
                        onBackClick = onBackClick,
                    )
                }

                AppBarSize.MEDIUM -> {
                    AnimatedMediumTopAppBar(
                        title = title,
                        colors = appBarColors,
                        onBackClick = { onBackClick?.invoke() },
                    )
                }
            }
        },
        floatingActionButton = floatingActionButton,
        contentWindowInsets = windowInsets,
        content = content,
    )
}