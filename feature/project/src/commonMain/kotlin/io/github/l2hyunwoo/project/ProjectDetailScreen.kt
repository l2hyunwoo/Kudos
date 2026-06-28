package io.github.l2hyunwoo.project

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.data.tasks.model.Task
import io.github.l2hyunwoo.kudos.core.common.compose.EventFlow
import io.github.l2hyunwoo.project.component.EditProjectBottomSheet
import io.github.l2hyunwoo.project.component.ProjectTasksList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    uiState: ProjectDetailUiState,
    eventFlow: EventFlow<ProjectDetailEvent>,
    onTaskClick: (Task) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val colors = KudosTheme.colors

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error.message ?: "오류가 발생했습니다",
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "프로젝트 상세",
                        style = KudosTheme.typography.titleMediumB,
                        color = colors.ink.ink,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { eventFlow.tryEmit(ProjectDetailEvent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기",
                            tint = colors.ink.ink2,
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { eventFlow.tryEmit(ProjectDetailEvent.ShowEditSheet) },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "수정",
                            tint = colors.brand.primary600,
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.glass.fill,
                        scrolledContainerColor = colors.glass.fill,
                    ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = colors.surface.bg,
        modifier = modifier,
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
            ) {
                CategoryBadge(
                    label = uiState.categoryPrefix,
                    categoryColor = uiState.categoryColor,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = uiState.title,
                    style = KudosTheme.typography.titleLargeB,
                    color = colors.ink.ink,
                )

                Spacer(modifier = Modifier.height(8.dp))

                uiState.description?.let { description ->
                    Text(
                        text = description,
                        style = KudosTheme.typography.bodyMediumR,
                        color = colors.ink.ink2,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                } ?: run {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                ProjectTasksList(
                    tasks = uiState.tasks,
                    onTaskClick = onTaskClick,
                )
            }

            if (uiState.isLoadingTasks || uiState.isUpdatingProject) {
                CircularProgressIndicator(
                    color = colors.brand.primary600,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }

    if (uiState.showEditSheet) {
        EditProjectBottomSheet(
            initialTitle = uiState.title,
            initialDescription = uiState.description,
            onDismiss = { eventFlow.tryEmit(ProjectDetailEvent.DismissEditSheet) },
            onUpdate = { request ->
                eventFlow.tryEmit(ProjectDetailEvent.UpdateProject(request))
            },
        )
    }
}

@Composable
private fun CategoryBadge(
    label: String,
    categoryColor: String,
    modifier: Modifier = Modifier,
) {
    val dot = remember(categoryColor) { parseHexColor(categoryColor) }
    val (bg, fg) = KudosTheme.colors.pastelChip(dot)
    Text(
        text = label,
        style = KudosTheme.typography.eyebrow,
        color = fg,
        modifier =
            modifier
                .clip(RoundedCornerShape(8.dp))
                .background(color = bg, shape = RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp),
    )
}

private fun parseHexColor(hex: String): Color {
    val value = hex.removePrefix("#").toLong(16) or 0xFF000000
    return Color(value)
}
