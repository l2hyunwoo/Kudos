package io.github.l2hyunwoo.kudos.core.soil

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.core.design.component.moon.MoonLoadingIndicator

@Composable
fun KudosSuspenseFallbackContents(modifier: Modifier = Modifier) {
    Box(
        modifier =
            modifier
                .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        MoonLoadingIndicator()
    }
}

@Preview
@Composable
private fun KudosSuspenseFallbackContentsPreview() {
    KudosTheme {
        KudosSuspenseFallbackContents()
    }
}
