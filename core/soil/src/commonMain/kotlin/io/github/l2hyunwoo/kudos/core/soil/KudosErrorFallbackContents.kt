package io.github.l2hyunwoo.kudos.core.soil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kudos.core.soil.generated.resources.Res
import kudos.core.soil.generated.resources.error
import kudos.core.soil.generated.resources.retry
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
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
        verticalArrangement = Arrangement.spacedBy(35.dp, Alignment.CenterVertically),
    ) {
        Text(
            text = stringResource(Res.string.error),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Button(
            onClick = { errorContext.errorBoundaryContext.reset?.invoke() },
            shapes = ButtonDefaults.shapes(),
        ) {
            Text(
                text = stringResource(Res.string.retry),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary,
            )
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
