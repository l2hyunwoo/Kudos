package io.github.l2hyunwoo.kudos.core.soil

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import io.github.l2hyunwoo.kudos.core.common.ScreenContext
import kotlinx.coroutines.launch
import soil.plant.compose.reacty.Await
import soil.plant.compose.reacty.ErrorBoundary
import soil.plant.compose.reacty.Suspense
import soil.query.compose.QueryObject
import soil.query.compose.SubscriptionObject
import soil.query.core.DataModel

@Composable
context(_: ScreenContext)
fun <T> SoilBoundary(
    state: DataModel<T>,
    modifier: Modifier = Modifier,
    fallback: SoilFallback = SoilFallbackDefaults.default(),
    content: @Composable (T) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    ErrorBoundary(
        fallback = fallback.errorFallback,
        onReset = {
            coroutineScope.launch {
                state.performResetIfNeeded()
            }
        },
        modifier = modifier,
    ) {
        Suspense(fallback = fallback.suspenseFallback) {
            Await(state = state, content = content)
        }
    }
}

@Composable
context(_: ScreenContext)
fun <T1, T2> SoilBoundary(
    state1: DataModel<T1>,
    state2: DataModel<T2>,
    modifier: Modifier = Modifier,
    fallback: SoilFallback = SoilFallbackDefaults.default(),
    content: @Composable (T1, T2) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    ErrorBoundary(
        fallback = fallback.errorFallback,
        onReset = {
            coroutineScope.launch {
                state1.performResetIfNeeded()
                state2.performResetIfNeeded()
            }
        },
        modifier = modifier,
    ) {
        Suspense(fallback = fallback.suspenseFallback) {
            Await(
                state1 = state1,
                state2 = state2,
                content = content,
            )
        }
    }
}

@Composable
context(_: ScreenContext)
fun <T1, T2, T3> SoilBoundary(
    state1: DataModel<T1>,
    state2: DataModel<T2>,
    state3: DataModel<T3>,
    modifier: Modifier = Modifier,
    fallback: SoilFallback = SoilFallbackDefaults.default(),
    content: @Composable (T1, T2, T3) -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()

    ErrorBoundary(
        fallback = fallback.errorFallback,
        onReset = {
            coroutineScope.launch {
                state1.performResetIfNeeded()
                state2.performResetIfNeeded()
                state3.performResetIfNeeded()
            }
        },
        modifier = modifier,
    ) {
        Suspense(fallback = fallback.suspenseFallback) {
            Await(
                state1 = state1,
                state2 = state2,
                state3 = state3,
                content = content,
            )
        }
    }
}


@Composable
private fun ErrorBoundary(
    modifier: Modifier = Modifier,
    fallback: @Composable context(ErrorContext) BoxScope.() -> Unit,
    onReset: (() -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    ErrorBoundary(
        fallback = {
            with(DefaultSoilErrorContext(it)) {
                fallback()
            }
        },
        onReset = onReset,
        modifier = modifier,
        content = content,
    )
}

@Composable
private fun Suspense(
    fallback: @Composable context(SuspenseContext) BoxScope.() -> Unit,
    content: @Composable () -> Unit,
) {
    Suspense(
        fallback = {
            with(DefaultSoilSuspenseContext()) {
                fallback()
            }
        },
        content = content,
    )
}

private suspend fun <T> DataModel<T>.performResetIfNeeded() {
    when (this) {
        is QueryObject<T> -> this.error?.let { this.refresh() }
        is SubscriptionObject<T> -> this.error?.let { this.reset() }
    }
}
