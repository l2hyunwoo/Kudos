package io.github.l2hyunwoo.category.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCategoryBottomSheet(
    onDismiss: () -> Unit,
    onCreate: (CreateCategoryRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()
    var prefix by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf<CategoryColor?>(null) }

    val isValid = prefix.isNotBlank() && title.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(Res.string.create_category),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = prefix,
                onValueChange = { prefix = it },
                label = { Text(stringResource(Res.string.prefix)) },
                placeholder = { Text(stringResource(Res.string.prefix_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(Res.string.title)) },
                placeholder = { Text(stringResource(Res.string.title_hint)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            ColorPicker(
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(Res.string.cancel))
                }

                Button(
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
                    enabled = isValid,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(Res.string.create))
                }
            }
        }
    }
}
