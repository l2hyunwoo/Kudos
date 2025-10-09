package io.github.l2hyunwoo.tasks.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
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
import io.github.l2hyunwoo.data.categories.model.Category
import io.github.l2hyunwoo.data.tasks.model.CreateTaskRequest
import io.github.l2hyunwoo.data.tasks.model.TaskPriority
import io.github.l2hyunwoo.data.tasks.model.TaskStatus
import kotlinx.collections.immutable.ImmutableList
import kudos.feature.tasks.generated.resources.Res
import kudos.feature.tasks.generated.resources.cancel
import kudos.feature.tasks.generated.resources.category
import kudos.feature.tasks.generated.resources.create
import kudos.feature.tasks.generated.resources.create_task
import kudos.feature.tasks.generated.resources.description
import kudos.feature.tasks.generated.resources.priority
import kudos.feature.tasks.generated.resources.select_category
import kudos.feature.tasks.generated.resources.status
import kudos.feature.tasks.generated.resources.title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateTaskBottomSheet(
    categories: ImmutableList<Category>,
    onDismiss: () -> Unit,
    onCreate: (CreateTaskRequest) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var selectedStatus by remember { mutableStateOf(TaskStatus.TODO) }
    var categoryDropdownExpanded by remember { mutableStateOf(false) }

    val isValid = selectedCategory != null && title.isNotBlank()

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
                text = stringResource(Res.string.create_task),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = categoryDropdownExpanded,
                onExpandedChange = { categoryDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCategory?.title ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(stringResource(Res.string.category)) },
                    placeholder = { Text(stringResource(Res.string.select_category)) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )

                ExposedDropdownMenu(
                    expanded = categoryDropdownExpanded,
                    onDismissRequest = { categoryDropdownExpanded = false }
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text("${category.prefix} - ${category.title}") },
                            onClick = {
                                selectedCategory = category
                                categoryDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(Res.string.title)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(Res.string.description)) },
                minLines = 3,
                maxLines = 5,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Priority Chips
            Text(
                text = stringResource(Res.string.priority),
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskPriority.entries.forEach { priority ->
                    FilterChip(
                        selected = selectedPriority == priority,
                        onClick = { selectedPriority = priority },
                        label = { Text(priority.text) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Status Chips
            Text(
                text = stringResource(Res.string.status),
                style = MaterialTheme.typography.labelLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskStatus.entries.forEach { status ->
                    FilterChip(
                        selected = selectedStatus == status,
                        onClick = { selectedStatus = status },
                        label = { Text("${status.text} ${status.name.lowercase().replace('_', ' ')}") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Action Buttons
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
                        selectedCategory?.let { category ->
                            onCreate(
                                CreateTaskRequest(
                                    categoryId = category.id,
                                    title = title,
                                    description = description.takeIf { it.isNotBlank() },
                                    priority = selectedPriority,
                                    status = selectedStatus
                                )
                            )
                            onDismiss()
                        }
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
