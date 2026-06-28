package io.github.l2hyunwoo.core.design.component.reveal

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.clipPath
import io.github.l2hyunwoo.core.design.token.LunarDurationMoonFill
import io.github.l2hyunwoo.core.design.token.LunarOvershootEasing
import io.github.l2hyunwoo.core.design.token.LocalReduceMotion
import io.github.l2hyunwoo.core.design.token.darkKudosColors
import io.github.l2hyunwoo.core.design.token.lightKudosColors

/**
 * Circular-reveal theme switch. The host flips the theme underneath this overlay, then [launch]es a
 * reveal: a solid scrim of the OUTGOING theme's background is painted over the whole window and a
 * circle punched at the tap origin grows to clear every corner, so the already-recomposed new theme
 * shows through the expanding hole.
 *
 * Lives ABOVE the theme on purpose (so it covers system bars + glass chrome), so it cannot read
 * KudosTheme.motion; it reads [LocalReduceMotion] directly and rebuilds the spec from the public
 * motion tokens. It records no subtree (flat scrim only), so it can never form a RenderNode cycle
 * with the Cloudy blur recorder.
 */
@Stable
class ThemeRevealState {
    // The toggle's window-space center and the outgoing theme. Read by the overlay while a reveal runs.
    internal var origin by mutableStateOf(Offset.Unspecified)
    internal var fromDark by mutableStateOf(false)

    // Monotonic generation: bumped on every launch so the driving effect re-keys even when a fixed
    // toggle button reports the SAME origin Offset twice in a row (structural equality would otherwise
    // make remember(origin)/LaunchedEffect(origin) a no-op on repeat taps and freeze the reveal).
    internal var generation by mutableStateOf(0)

    // The hole radius, 0f..1f progress, owned here so a single Animatable survives across taps and is
    // re-targeted per launch — never recreated mid-flight.
    internal val progress = Animatable(0f)

    internal val active: Boolean get() = origin.isSpecified

    // Must be called from the UI/event thread (a composition event lambda) so this arming and the
    // host's theme flip commit in the same snapshot — the recomposition that observes the new theme
    // also observes the armed reveal, so there is no torn frame.
    fun launch(origin: Offset, fromDark: Boolean) {
        this.fromDark = fromDark
        this.origin = origin
        generation += 1
    }
}

@Composable
fun rememberThemeRevealState(): ThemeRevealState = remember { ThemeRevealState() }

@Composable
fun CircularRevealThemeSwitch(
    state: ThemeRevealState,
    modifier: Modifier = Modifier,
) {
    if (!state.active) return // idle: zero cost

    val reduceMotion = LocalReduceMotion.current
    // Outgoing-theme background, resolved outside KudosTheme via the same plain factories Theme.kt uses.
    val scrimColor = remember(state.fromDark) {
        if (state.fromDark) darkKudosColors().surface.bg else lightKudosColors().surface.bg
    }

    // Keyed on the generation counter (not the origin), so every tap restarts the animation even when
    // the fixed toggle reports an identical origin.
    LaunchedEffect(state.generation) {
        val spec: AnimationSpec<Float> =
            if (reduceMotion) snap() else tween(LunarDurationMoonFill, easing = LunarOvershootEasing)
        state.progress.snapTo(0f)
        state.progress.animateTo(1f, spec)
        state.origin = Offset.Unspecified // tear down -> overlay returns early next frame
    }

    val origin = state.origin
    Canvas(modifier) {
        // Read the animated value INSIDE the draw lambda: a snapshot read here re-runs draw (cheap),
        // never recomposition. The hole must clear the farthest corner, padded so the easing's settle
        // leaves no residual sliver at the near corner on the final frame.
        val maxRadius = REVEAL_RADIUS_PADDING * listOf(
            Offset.Zero,
            Offset(size.width, 0f),
            Offset(0f, size.height),
            Offset(size.width, size.height),
        ).maxOf { (it - origin).getDistance() }
        val hole = Path().apply { addOval(Rect(origin, state.progress.value * maxRadius)) }
        clipPath(hole, clipOp = ClipOp.Difference) {
            drawRect(scrimColor)
        }
    }
}

// Over-grow the hole slightly past the farthest corner so a non-overshoot easing can't leave a 1px
// scrim ring at the near corner on the last frame.
private const val REVEAL_RADIUS_PADDING = 1.02f
