package io.github.l2hyunwoo.kudos.core.soil

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.l2hyunwoo.core.design.safeDrawingWithBottomNavBar
import io.github.l2hyunwoo.kudos.core.soil.appbar.AppBarFallbackScaffold
import io.github.l2hyunwoo.kudos.core.soil.appbar.AppBarSize

sealed interface SoilFallback {
    val suspenseFallback: @Composable context(SuspenseContext) BoxScope.() -> Unit
    val errorFallback: @Composable context(ErrorContext) BoxScope.() -> Unit
}

object SoilFallbackDefaults {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun appBar(
        title: String,
        colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors().copy(
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
        onBackClick: (() -> Unit)? = null,
        appBarSize: AppBarSize = AppBarSize.DEFAULT,
        // Allowing WindowInsets to be overridden to prevent layout jump/glitches
        // when navigating between screens with and without a bottom navigation bar.
        windowInsets: WindowInsets = WindowInsets.safeDrawingWithBottomNavBar,
        contentBackground: (@Composable (innerPadding: PaddingValues) -> Unit)? = null,
    ): SoilFallback = AppBar(
        title = title,
        colors = colors,
        onBackClick = onBackClick,
        size = appBarSize,
        windowInsets = windowInsets,
        contentBackground = contentBackground,
    )

    fun default(): SoilFallback = Default

    fun custom(
        suspenseFallback: @Composable context(SuspenseContext) BoxScope.() -> Unit,
        errorFallback: @Composable context(ErrorContext) BoxScope.() -> Unit,
    ): SoilFallback = Custom(
        suspenseFallback = suspenseFallback,
        errorFallback = errorFallback,
    )
}

private object Default : SoilFallback {
    override val suspenseFallback: @Composable context(SuspenseContext) BoxScope.() -> Unit
        get() = { KudosSuspenseFallbackContents() }
    override val errorFallback: @Composable context(ErrorContext) BoxScope.() -> Unit
        get() = { KudosErrorFallbackContents() }
}

private class Custom(
    override val suspenseFallback: @Composable context(SuspenseContext) BoxScope.() -> Unit,
    override val errorFallback: @Composable context(ErrorContext) BoxScope.() -> Unit,
) : SoilFallback

@OptIn(ExperimentalMaterial3Api::class)
private class AppBar(
    val title: String,
    val colors: TopAppBarColors,
    val onBackClick: (() -> Unit)?,
    val size: AppBarSize,
    val windowInsets: WindowInsets,
    val contentBackground: (@Composable (innerPadding: PaddingValues) -> Unit)?,
) : SoilFallback {
    override val suspenseFallback: @Composable context(SuspenseContext) BoxScope.() -> Unit = {
        AppBarFallbackScaffold(
            title = title,
            onBackClick = onBackClick,
            appBarSize = size,
            appBarColors = colors,
            windowInsets = windowInsets,
        ) { innerPadding ->
            contentBackground?.invoke(innerPadding)
            KudosSuspenseFallbackContents(
                modifier = Modifier.padding(innerPadding),
            )
        }
    }

    override val errorFallback: @Composable context(ErrorContext) BoxScope.() -> Unit = {
        AppBarFallbackScaffold(
            title = title,
            onBackClick = onBackClick,
            appBarSize = size,
            appBarColors = colors,
            windowInsets = windowInsets,
        ) { innerPadding ->
            contentBackground?.invoke(innerPadding)
            KudosErrorFallbackContents(
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}
