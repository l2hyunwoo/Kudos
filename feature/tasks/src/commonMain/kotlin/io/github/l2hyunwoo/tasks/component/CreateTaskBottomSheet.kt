package io.github.l2hyunwoo.tasks.component

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.core.design.component.button.GhostButton
import io.github.l2hyunwoo.core.design.component.button.PrimaryButton
import io.github.l2hyunwoo.core.design.component.sheet.KudosBottomSheet
import io.github.l2hyunwoo.data.categories.model.Category
import io.github.l2hyunwoo.data.categories.model.Project
import io.github.l2hyunwoo.data.tasks.model.CreateTaskRequest
import io.github.l2hyunwoo.data.tasks.model.TaskPriority
import io.github.l2hyunwoo.data.tasks.model.TaskStatus
import io.github.l2hyunwoo.tasks.DueOption
import io.github.l2hyunwoo.tasks.dueOptionToIso
import io.github.l2hyunwoo.tasks.isoFromEpochDayDue
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import kudos.feature.tasks.generated.resources.Res
import kudos.feature.tasks.generated.resources.add_description
import kudos.feature.tasks.generated.resources.cancel
import kudos.feature.tasks.generated.resources.category
import kudos.feature.tasks.generated.resources.create
import kudos.feature.tasks.generated.resources.create_task
import kudos.feature.tasks.generated.resources.date_confirm
import kudos.feature.tasks.generated.resources.due_date
import kudos.feature.tasks.generated.resources.due_none
import kudos.feature.tasks.generated.resources.due_pick
import kudos.feature.tasks.generated.resources.due_this_week
import kudos.feature.tasks.generated.resources.due_today
import kudos.feature.tasks.generated.resources.no_project
import kudos.feature.tasks.generated.resources.priority
import kudos.feature.tasks.generated.resources.project
import kudos.feature.tasks.generated.resources.status
import kudos.feature.tasks.generated.resources.status_backlog
import kudos.feature.tasks.generated.resources.status_done
import kudos.feature.tasks.generated.resources.status_in_progress
import kudos.feature.tasks.generated.resources.status_todo
import kudos.feature.tasks.generated.resources.task_title_hint
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
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
    val scope = rememberCoroutineScope()
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedProject by remember { mutableStateOf<Project?>(null) }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var selectedStatus by remember { mutableStateOf(TaskStatus.TODO) }
    var selectedDue by remember { mutableStateOf(DueOption.NONE) }
    var pickedIso by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val availableProjects = remember(selectedCategory) {
        selectedCategory?.projects ?: emptyList()
    }

    LaunchedEffect(selectedCategory) {
        if (selectedProject != null && !availableProjects.contains(selectedProject)) {
            selectedProject = null
        }
    }

    val isValid = selectedCategory != null && title.isNotBlank()

    KudosBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
            ) {
                Text(
                    text = stringResource(Res.string.create_task),
                    style = KudosTheme.typography.bodyLargeXB,
                    color = KudosTheme.colors.ink.ink,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Headline title + description first (matches the mockup's "title first" feel), then the
                // functionally-required selectors below.
                LunarTextInput(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = stringResource(Res.string.task_title_hint),
                    textStyle = KudosTheme.typography.titleLargeM,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                LunarTextInput(
                    value = description,
                    onValueChange = { description = it },
                    placeholder = stringResource(Res.string.add_description),
                    textStyle = KudosTheme.typography.bodyLargeR,
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // CATEGORY drives PROJECT, so it leads the selector block.
                LunarFormSection(title = stringResource(Res.string.category).uppercase()) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { category ->
                            LunarSelectableChip(
                                label = "${category.prefix} · ${category.title}",
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LunarFormSection(title = stringResource(Res.string.project).uppercase()) {
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LunarSelectableChip(
                            label = stringResource(Res.string.no_project),
                            selected = selectedProject == null,
                            onClick = { selectedProject = null }
                        )
                        availableProjects.forEach { project ->
                            LunarSelectableChip(
                                label = project.title,
                                selected = selectedProject == project,
                                onClick = { selectedProject = project }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LunarFormSection(title = stringResource(Res.string.status).uppercase()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TaskStatus.entries.forEach { status ->
                            LunarStatusCard(
                                fraction = status.fraction,
                                label = stringResource(status.labelRes()),
                                selected = selectedStatus == status,
                                onClick = { selectedStatus = status },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LunarFormSection(title = stringResource(Res.string.priority).uppercase()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TaskPriority.entries.forEach { priority ->
                            LunarSelectableChip(
                                label = priority.text,
                                selected = selectedPriority == priority,
                                onClick = { selectedPriority = priority },
                                leadingDotColor = priority.dotColor()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                LunarFormSection(title = stringResource(Res.string.due_date).uppercase()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LunarSelectableChip(
                            label = stringResource(Res.string.due_today),
                            selected = selectedDue == DueOption.TODAY,
                            onClick = { selectedDue = DueOption.TODAY }
                        )
                        LunarSelectableChip(
                            label = stringResource(Res.string.due_this_week),
                            selected = selectedDue == DueOption.THIS_WEEK,
                            onClick = { selectedDue = DueOption.THIS_WEEK }
                        )
                        LunarSelectableChip(
                            // Show the chosen date on the Pick chip once one exists.
                            label = pickedIso ?: stringResource(Res.string.due_pick),
                            selected = selectedDue == DueOption.PICK,
                            onClick = {
                                selectedDue = DueOption.PICK
                                showDatePicker = true
                            }
                        )
                        LunarSelectableChip(
                            label = stringResource(Res.string.due_none),
                            selected = selectedDue == DueOption.NONE,
                            onClick = { selectedDue = DueOption.NONE }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp, top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GhostButton(
                    onClick = {
                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                onDismiss()
                            }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(stringResource(Res.string.cancel))
                }

                PrimaryButton(
                    onClick = {
                        selectedCategory?.let { category ->
                            onCreate(
                                CreateTaskRequest(
                                    categoryId = category.id,
                                    title = title,
                                    description = description.takeIf { it.isNotBlank() },
                                    projectId = selectedProject?.id,
                                    priority = selectedPriority,
                                    status = selectedStatus,
                                    dueDate = dueOptionToIso(selectedDue, pickedIso)
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

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            // selectedDateMillis is the start-of-day in UTC, so integer division by a
                            // day's millis yields the exact epoch day.
                            pickedIso = isoFromEpochDayDue(millis / 86_400_000L)
                            selectedDue = DueOption.PICK
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(Res.string.date_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

// Status enum -> localized label resource.
private fun TaskStatus.labelRes(): StringResource = when (this) {
    TaskStatus.BACKLOG -> Res.string.status_backlog
    TaskStatus.TODO -> Res.string.status_todo
    TaskStatus.IN_PROGRESS -> Res.string.status_in_progress
    TaskStatus.DONE -> Res.string.status_done
}

// Priority enum -> its semantic color token (the leading dot on the priority chip).
@Composable
private fun TaskPriority.dotColor() = when (this) {
    TaskPriority.URGENT -> KudosTheme.colors.priority.urgent
    TaskPriority.HIGH -> KudosTheme.colors.priority.high
    TaskPriority.MEDIUM -> KudosTheme.colors.priority.medium
    TaskPriority.LOW -> KudosTheme.colors.priority.low
}
