package io.github.l2hyunwoo.core.design.transition

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier

// Shared-element wiring for the list-row -> task-detail transition. Hosted here (core:design) because
// every feature module depends on core:design via the feature convention plugin, so both the list row
// (feature:tasks) and the detail screen (feature:tasks) — plus :shared, which provides the scopes —
// can see these without a cross-module dependency.
//
// Both locals default to null and every read null-checks: a leaf composable used standalone (preview,
// a screen not hosted under SharedTransitionLayout) renders without the shared modifier. This is also
// the iOS safety contract: reading a non-null scope that was never provided crashes on iOS when a
// detail route sits on the back stack, so the scope must be genuinely absent (null), not asserted.
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }
val LocalNavAnimatedContentScope = compositionLocalOf<AnimatedVisibilityScope?> { null }

// Applies a shared-element/shared-bounds modifier keyed on [key], or returns the modifier unchanged
// when either scope is absent (standalone/preview usage). Use bounds=true when the two endpoints
// differ in size/style (the title) so the bounds are tweened; use the default (sharedElement) when the
// element is the same glyph at both ends (the moon, the id label).
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Modifier.sharedTask(
    key: String,
    bounds: Boolean = false,
): Modifier {
    val transition = LocalSharedTransitionScope.current ?: return this
    val animatedContent = LocalNavAnimatedContentScope.current ?: return this
    return with(transition) {
        if (bounds) {
            sharedBounds(rememberSharedContentState(key), animatedContent)
        } else {
            sharedElement(rememberSharedContentState(key), animatedContent)
        }
    }
}
