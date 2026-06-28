package io.github.l2hyunwoo.main.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.isSpecified
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme

@Composable
internal fun ThemeToggleButton(
    darkTheme: Boolean,
    onToggle: (Offset) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interaction = remember { MutableInteractionSource() }
    // Window-space center of this button; written by layout, read only at click so reflow never
    // recomposes. Emitted on tap so the App-root reveal overlay (also in window space) can center
    // the expanding circle here.
    var center by remember { mutableStateOf(Offset.Unspecified) }
    Box(
        modifier
            .size(36.dp)
            .onGloballyPositioned { center = it.boundsInWindow().center }
            // clip precedes clickable so the bounded ripple is masked to the circle.
            .clip(PillShape)
            .background(KudosTheme.colors.brand.primary050)
            .clickable(
                interactionSource = interaction,
                indication = ripple(bounded = true),
                onClick = { if (center.isSpecified) onToggle(center) },
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Icon shows the mode the tap switches TO: a sun while dark, a moon while light.
        Icon(
            imageVector = if (darkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
            contentDescription = if (darkTheme) "밝은 테마로 전환" else "어두운 테마로 전환",
            tint = KudosTheme.colors.brand.primary500,
            modifier = Modifier.size(20.dp),
        )
    }
}
