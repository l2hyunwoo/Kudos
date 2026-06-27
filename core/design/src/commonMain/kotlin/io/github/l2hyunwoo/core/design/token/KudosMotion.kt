package io.github.l2hyunwoo.core.design.token

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf

val LunarStandardEasing: Easing = CubicBezierEasing(0.32f, 0.72f, 0f, 1f)
val LunarEmphasizedEasing: Easing = CubicBezierEasing(0.2f, 0f, 0f, 1f)
val LunarOvershootEasing: Easing = CubicBezierEasing(0.34f, 1.4f, 0.64f, 1f)

const val LunarDurationMicro = 120
const val LunarDurationStandard = 240
const val LunarDurationScreenPush = 360
const val LunarDurationSheet = 520
const val LunarDurationMoonFill = 520

@Immutable
data class KudosMotion(
    val micro: FiniteAnimationSpec<Float>,
    val standard: FiniteAnimationSpec<Float>,
    val screenPush: FiniteAnimationSpec<Float>,
    val sheet: FiniteAnimationSpec<Float>,
    val moonFill: FiniteAnimationSpec<Float>,
)

// reduceMotion collapses the large/expressive specs to a short cross-fade; micro stays as-is.
fun kudosMotion(reduceMotion: Boolean): KudosMotion {
    val reduced: FiniteAnimationSpec<Float> = tween(LunarDurationMicro, easing = LunarStandardEasing)
    return KudosMotion(
        micro = tween(LunarDurationMicro, easing = LunarStandardEasing),
        standard = if (reduceMotion) reduced else tween(LunarDurationStandard, easing = LunarStandardEasing),
        screenPush = if (reduceMotion) reduced else tween(LunarDurationScreenPush, easing = LunarEmphasizedEasing),
        sheet = if (reduceMotion) reduced else tween(LunarDurationSheet, easing = LunarEmphasizedEasing),
        moonFill = if (reduceMotion) reduced else tween(LunarDurationMoonFill, easing = LunarOvershootEasing),
    )
}

// Best-effort system flag; platforms may override at the theme root. Default off.
val LocalReduceMotion = staticCompositionLocalOf { false }

val LocalMotion = staticCompositionLocalOf { kudosMotion(reduceMotion = false) }
