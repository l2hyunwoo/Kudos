package io.github.l2hyunwoo.tasks.detail.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.core.design.component.button.GhostButton
import io.github.l2hyunwoo.core.design.component.button.PrimaryButton
import io.github.l2hyunwoo.core.design.component.moon.Moon
import io.github.l2hyunwoo.core.design.component.sheet.KudosBottomSheet
import io.github.l2hyunwoo.data.tasks.model.TaskPriority
import io.github.l2hyunwoo.data.tasks.model.TaskStatus
import io.github.l2hyunwoo.data.tasks.model.UpdateTaskRequest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditTaskBottomSheet(
    initialTitle: String,
    initialDescription: String?,
    initialStatus: TaskStatus,
    initialPriority: TaskPriority,
    initialDueDate: String?,
    onDismiss: () -> Unit,
    onUpdate: (UpdateTaskRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()

    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription ?: "") }
    var dueDate by remember { mutableStateOf(initialDueDate ?: "") }
    var selectedStatus by remember { mutableStateOf(initialStatus) }
    var selectedPriority by remember { mutableStateOf(initialPriority) }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = KudosTheme.colors.brand.primary600,
        unfocusedBorderColor = KudosTheme.colors.surface.outlineStrong,
        focusedLabelColor = KudosTheme.colors.brand.primary600,
        cursorColor = KudosTheme.colors.brand.primary600,
    )

    KudosBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Edit Task",
                style = KudosTheme.typography.titleLargeB,
                color = KudosTheme.colors.ink.ink,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true,
                shape = KudosTheme.shapes.chipSmall,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                minLines = 3,
                maxLines = 5,
                shape = KudosTheme.shapes.chipSmall,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = dueDate,
                onValueChange = { dueDate = it },
                label = { Text("Due date (ISO-8601)") },
                singleLine = true,
                shape = KudosTheme.shapes.chipSmall,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "PRIORITY",
                style = KudosTheme.typography.eyebrow,
                color = KudosTheme.colors.ink.ink3,
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskPriority.entries.forEach { priority ->
                    PriorityChip(
                        priority = priority,
                        isSelected = selectedPriority == priority,
                        onClick = { selectedPriority = priority },
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "STATUS",
                style = KudosTheme.typography.eyebrow,
                color = KudosTheme.colors.ink.ink3,
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TaskStatus.entries.forEach { status ->
                    StatusChip(
                        status = status,
                        isSelected = selectedStatus == status,
                        onClick = { selectedStatus = status },
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                GhostButton(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Spacer(modifier = Modifier.weight(0.2f))

                PrimaryButton(
                    onClick = {
                        onUpdate(
                            UpdateTaskRequest(
                                title = title,
                                description = description.ifBlank { null },
                                status = selectedStatus,
                                priority = selectedPriority,
                                dueDate = dueDate.ifBlank { null },
                            )
                        )
                    },
                    enabled = title.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun PriorityChip(
    priority: TaskPriority,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val p = KudosTheme.colors.priority
    val dot = when (priority) {
        TaskPriority.URGENT -> p.urgent
        TaskPriority.HIGH -> p.high
        TaskPriority.MEDIUM -> p.medium
        TaskPriority.LOW -> p.low
    }
    val (bg, fg) = KudosTheme.colors.pastelChip(dot)
    Text(
        text = priority.text,
        style = KudosTheme.typography.labelLargeM,
        color = if (isSelected) Color.White else fg,
        modifier = modifier
            .clip(KudosTheme.shapes.chipSmall)
            .background(if (isSelected) dot else bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
    )
}

@Composable
private fun StatusChip(
    status: TaskStatus,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (isSelected) KudosTheme.colors.brand.primary100 else KudosTheme.colors.surface.surface2
    val fg = if (isSelected) KudosTheme.colors.brand.primary600 else KudosTheme.colors.ink.ink2
    Row(
        modifier = modifier
            .clip(KudosTheme.shapes.chipSmall)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Moon(k = status.fraction, size = 18.dp)
        Text(
            text = status.label(),
            style = KudosTheme.typography.labelLargeM,
            color = fg,
        )
    }
}

private fun TaskStatus.label(): String = when (this) {
    TaskStatus.BACKLOG -> "백로그"
    TaskStatus.TODO -> "할 일"
    TaskStatus.IN_PROGRESS -> "진행 중"
    TaskStatus.DONE -> "완료"
}
