package io.github.l2hyunwoo.kudos.core.soil

import soil.plant.compose.reacty.ErrorBoundaryContext

interface FallbackContext

interface SuspenseContext : FallbackContext

interface ErrorContext : FallbackContext {
    val errorBoundaryContext: ErrorBoundaryContext
}

internal class DefaultSoilSuspenseContext : SuspenseContext

internal class DefaultSoilErrorContext(
    override val errorBoundaryContext: ErrorBoundaryContext,
) : ErrorContext

interface SoilPreviewContext : SuspenseContext, ErrorContext

internal class FakePreviewContext : SoilPreviewContext {
    override val errorBoundaryContext: ErrorBoundaryContext = ErrorBoundaryContext(
        err = Throwable("Fake error for preview"),
        reset = null,
    )
}

