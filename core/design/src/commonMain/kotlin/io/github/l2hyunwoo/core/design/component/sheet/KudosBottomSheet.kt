package io.github.l2hyunwoo.core.design.component.sheet

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme

/**
 * Shared frosted bottom-sheet for the Lunar glass language.
 *
 * The design spec (section 05) classes bottom sheets as *glass chrome* that floats over content.
 * The app's real backdrop blur lives in `Modifier.glassSurface(sky)`, which works by blurring a
 * `sky` backdrop that the scrollable content records via `Modifier.sky(sky)`. A [ModalBottomSheet]
 * renders in a SEPARATE window/popup above the app content, so it has no access to the screen's
 * `sky` recorder — and Compose's `Modifier.blur`/RenderEffect on the sheet would only blur the
 * sheet's OWN content, never the dimmed content behind it. A true backdrop-blur sheet is therefore
 * not achievable with the current `glassSurface(sky)` approach.
 *
 * So this wrapper does the honest, achievable thing: a translucent-frost slab in the same material
 * family as the glass chrome, WITHOUT claiming a backdrop blur it can't do. It gives every sheet a
 * consistent look:
 *  - [KudosTheme.colors] `glass.sheetFill` container — a high-alpha periwinkle-tinted frosted fill.
 *  - [KudosTheme.shapes] `sheet` (28dp top radius).
 *  - a glass drag handle (periwinkle-tinted, replacing Material's neutral one).
 *  - a soft periwinkle-tinted dark `scrim`.
 *
 * Centralizing the params here keeps all sheets matched instead of copy-pasting `containerColor` /
 * `scrimColor` / `shape` at each call site. Callers supply their own content (header, fields,
 * actions) exactly as they would to a raw [ModalBottomSheet].
 *
 * @param onDismissRequest invoked when the user dismisses the sheet (scrim tap, back, swipe down).
 * @param sheetState hoist if the caller animates `hide()` before dismissing; defaults to a
 *   fully-expanded state to match the existing sheets (`skipPartiallyExpanded = true`).
 * @param shape sheet outline; defaults to the 28dp Lunar sheet shape.
 * @param content sheet body (a ColumnScope), identical contract to [ModalBottomSheet].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KudosBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    shape: Shape = KudosTheme.shapes.sheet,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = shape,
        containerColor = KudosTheme.colors.glass.sheetFill,
        contentColor = KudosTheme.colors.ink.ink,
        scrimColor = KudosTheme.colors.glass.scrim,
        dragHandle = { GlassDragHandle() },
        modifier = modifier,
        content = content,
    )
}

/**
 * Periwinkle-tinted pill drag handle, replacing Material's neutral grey one so the grab affordance
 * reads as part of the glass material family.
 */
@Composable
private fun GlassDragHandle() {
    val handleDescription = "Drag handle"
    Surface(
        modifier =
            Modifier
                .padding(vertical = 12.dp)
                .semantics { contentDescription = handleDescription },
        color = KudosTheme.colors.brand.primary200,
        shape = RoundedCornerShape(999.dp),
    ) {
        Box(Modifier.size(width = 36.dp, height = 4.dp))
    }
}
