package io.github.l2hyunwoo.tasks.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme

/**
 * Borderless Lunar text field built on [BasicTextField] (commonMain foundation, works on all targets).
 * No Material container/outline — just ink-colored text on a periwinkle cursor, with a [placeholder]
 * overlaid when [value] is empty. Use for the headline title and description inputs in the sheet.
 *
 * @param textStyle the input text style; merged with the ink color. The placeholder reuses it at ink3.
 */
@Composable
fun LunarTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = KudosTheme.typography.titleLargeM,
    singleLine: Boolean = false,
    minLines: Int = 1,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = textStyle.merge(color = KudosTheme.colors.ink.ink),
        cursorBrush = SolidColor(KudosTheme.colors.brand.primary600),
        singleLine = singleLine,
        minLines = minLines,
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = textStyle,
                        color = KudosTheme.colors.ink.ink3,
                    )
                }
                innerTextField()
            }
        },
    )
}

@Preview
@Composable
private fun LunarTextInputLightPreview() {
    KudosTheme(darkTheme = false) {
        LunarTextInputPreviewColumn()
    }
}

@Preview
@Composable
private fun LunarTextInputDarkPreview() {
    KudosTheme(darkTheme = true) {
        LunarTextInputPreviewColumn()
    }
}

@Composable
private fun LunarTextInputPreviewColumn() {
    Column(
        modifier = Modifier
            .padding(16.dp),
    ) {
        LunarTextInput(
            value = "Ship the release",
            onValueChange = {},
            placeholder = "What needs to be done?",
            textStyle = KudosTheme.typography.titleLargeM,
            singleLine = true,
        )
        LunarTextInput(
            value = "",
            onValueChange = {},
            placeholder = "Add description…",
            textStyle = KudosTheme.typography.bodyLargeR,
            minLines = 3,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}
