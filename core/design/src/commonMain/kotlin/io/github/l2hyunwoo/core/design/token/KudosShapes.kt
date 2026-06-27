package io.github.l2hyunwoo.core.design.token

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class KudosShapes(
    val row: RoundedCornerShape = RoundedCornerShape(16.dp),
    val card: RoundedCornerShape = RoundedCornerShape(20.dp),
    val sheet: RoundedCornerShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    val pill: RoundedCornerShape = RoundedCornerShape(999.dp),
    val chipSmall: RoundedCornerShape = RoundedCornerShape(14.dp),
) {
    companion object {
        val RowRadius: Dp = 16.dp
        val CardRadius: Dp = 20.dp
        val SheetRadius: Dp = 28.dp
        val PillRadius: Dp = 999.dp
        val ChipSmallRadius: Dp = 14.dp
    }
}

val LocalShapes = staticCompositionLocalOf { KudosShapes() }
