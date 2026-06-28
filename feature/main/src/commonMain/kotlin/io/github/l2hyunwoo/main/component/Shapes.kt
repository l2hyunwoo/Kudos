package io.github.l2hyunwoo.main.component

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.token.KudosShapes

// Full pill: PillRadius (999dp) caps to half the smaller dimension for any chrome height.
internal val PillShape = RoundedCornerShape(KudosShapes.PillRadius)

// Glass header outline: meets the screen top edge (square top), soft-rounded bottom so the frosted
// panel reads as a sheet hanging from the top rather than a free-floating card.
internal val HeaderShape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
