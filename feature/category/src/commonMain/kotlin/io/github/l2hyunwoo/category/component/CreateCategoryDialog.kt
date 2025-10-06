package io.github.l2hyunwoo.category.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.data.categories.model.CategoryColor
import io.github.l2hyunwoo.data.categories.model.CreateCategoryRequest
import kudos.feature.category.generated.resources.Res
import kudos.feature.category.generated.resources.cancel
import kudos.feature.category.generated.resources.create
import kudos.feature.category.generated.resources.create_category
import kudos.feature.category.generated.resources.prefix
import kudos.feature.category.generated.resources.prefix_hint
import kudos.feature.category.generated.resources.title
import kudos.feature.category.generated.resources.title_hint
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateCategoryDialog(
    onDismiss: () -> Unit,
    onCreate: (CreateCategoryRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    var prefix by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf<CategoryColor?>(null) }

    val isValid = prefix.isNotBlank() && title.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.create_category)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = prefix,
                    onValueChange = { prefix = it },
                    label = { Text(stringResource(Res.string.prefix)) },
                    placeholder = { Text(stringResource(Res.string.prefix_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(Res.string.title)) },
                    placeholder = { Text(stringResource(Res.string.title_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                ColorPicker(
                    selectedColor = selectedColor,
                    onColorSelected = { selectedColor = it },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCreate(
                        CreateCategoryRequest(
                            prefix = prefix,
                            title = title,
                            color = selectedColor?.hexCode
                        )
                    )
                    onDismiss()
                },
                enabled = isValid
            ) {
                Text(stringResource(Res.string.create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        },
        modifier = modifier
    )
}
