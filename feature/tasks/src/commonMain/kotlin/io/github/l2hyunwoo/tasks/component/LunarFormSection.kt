package io.github.l2hyunwoo.tasks.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme

/**
 * A labelled form section: an uppercase eyebrow label over a content slot. Pass an already-uppercased
 * [title] (e.g. `stringResource(...).uppercase()`) to match the existing eyebrow usage in the sheet.
 */
@Composable
fun LunarFormSection(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier) {
        Text(
            text = title,
            style = KudosTheme.typography.eyebrow,
            color = KudosTheme.colors.ink.ink3,
        )
        Spacer(Modifier.height(8.dp))
        content()
    }
}

@Preview
@Composable
private fun LunarFormSectionLightPreview() {
    KudosTheme(darkTheme = false) {
        LunarFormSectionPreview()
    }
}

@Preview
@Composable
private fun LunarFormSectionDarkPreview() {
    KudosTheme(darkTheme = true) {
        LunarFormSectionPreview()
    }
}

@Composable
private fun LunarFormSectionPreview() {
    LunarFormSection(
        title = "DUE DATE",
        modifier =
            Modifier
                .background(KudosTheme.colors.surface.bg)
                .padding(16.dp),
    ) {
        Text(
            text = "Section content goes here",
            style = KudosTheme.typography.bodyLargeR,
            color = KudosTheme.colors.ink.ink,
        )
    }
}
