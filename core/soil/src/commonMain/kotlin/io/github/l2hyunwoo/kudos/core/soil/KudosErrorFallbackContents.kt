package io.github.l2hyunwoo.kudos.core.soil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.core.design.component.button.PrimaryButton
import kudos.core.soil.generated.resources.Res
import kudos.core.soil.generated.resources.error
import kudos.core.soil.generated.resources.retry
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview

@Composable
context(errorContext: ErrorContext)
fun KudosErrorFallbackContents(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = stringResource(Res.string.error),
            style = KudosTheme.typography.titleLargeB,
            color = KudosTheme.colors.ink.ink,
        )
        PrimaryButton(
            onClick = { errorContext.errorBoundaryContext.reset?.invoke() },
        ) {
            Text(text = stringResource(Res.string.retry))
        }
    }
}

@Preview
@Composable
private fun DefaultErrorFallbackContentPreview() {
    KudosSoilPreviewContainer {
        KudosErrorFallbackContents()
    }
}
