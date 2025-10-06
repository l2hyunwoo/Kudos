package io.github.l2hyunwoo.kudos.core.soil

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.l2hyunwoo.core.design.KudosTheme

@Composable
fun KudosSoilPreviewContainer(
    modifier: Modifier = Modifier,
    content: @Composable context(SoilPreviewContext) () -> Unit,
) {
    KudosTheme {
        Surface(modifier = modifier) {
            with(FakePreviewContext()) {
                content()
            }
        }
    }
}
