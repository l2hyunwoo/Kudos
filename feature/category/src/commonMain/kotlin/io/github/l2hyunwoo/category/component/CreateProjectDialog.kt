package io.github.l2hyunwoo.category.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import io.github.l2hyunwoo.data.categories.model.CreateProjectRequest
import kudos.feature.category.generated.resources.Res
import kudos.feature.category.generated.resources.cancel
import kudos.feature.category.generated.resources.create
import kudos.feature.category.generated.resources.create_project
import kudos.feature.category.generated.resources.description
import kudos.feature.category.generated.resources.description_hint
import kudos.feature.category.generated.resources.title
import kudos.feature.category.generated.resources.title_hint
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreateProjectDialog(
    categoryId: String,
    onDismiss: () -> Unit,
    onCreate: (String, CreateProjectRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    val isValid = title.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.create_project)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(Res.string.title)) },
                    placeholder = { Text(stringResource(Res.string.title_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text(stringResource(Res.string.description)) },
                    placeholder = { Text(stringResource(Res.string.description_hint)) },
                    minLines = 3,
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCreate(
                        categoryId,
                        CreateProjectRequest(
                            title = title,
                            description = description.takeIf { it.isNotBlank() }
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
