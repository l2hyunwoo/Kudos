package io.github.l2hyunwoo.kudos.core.soil

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.github.l2hyunwoo.core.design.KudosTheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun KudosSuspenseFallbackContents(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator()
    }
}

@Preview
@Composable
private fun KudosSuspenseFallbackContentsPreview() {
    KudosTheme {
        KudosSuspenseFallbackContents()
    }
}
