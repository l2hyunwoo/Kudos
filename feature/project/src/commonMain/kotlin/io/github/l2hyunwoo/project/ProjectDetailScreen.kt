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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.kudos.core.common.compose.EventFlow
import io.github.l2hyunwoo.project.component.EditProjectBottomSheet
import io.github.l2hyunwoo.project.component.ProjectTasksList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
    uiState: ProjectDetailUiState,
    eventFlow: EventFlow<ProjectDetailEvent>,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error.message ?: "오류가 발생했습니다"
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("프로젝트 상세") },
                navigationIcon = {
                    IconButton(onClick = { eventFlow.tryEmit(ProjectDetailEvent.NavigateBack) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { eventFlow.tryEmit(ProjectDetailEvent.ShowEditSheet) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "수정"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Category Badge
                Text(
                    text = uiState.categoryPrefix,
                    style = KudosTheme.typography.labelSmallM,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            color = Color(uiState.categoryColor.removePrefix("#").toLong(16) or 0xFF000000),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clip(RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Project Title
                Text(
                    text = uiState.title,
                    style = KudosTheme.typography.titleLargeB,
                    color = KudosTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Project Description
                uiState.description?.let { description ->
                    Text(
                        text = description,
                        style = KudosTheme.typography.bodyMediumR,
                        color = KudosTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                } ?: run {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Tasks List
                ProjectTasksList(
                    tasks = uiState.tasks,
                    onTaskClick = { /* TODO: Navigate to task detail */ }
                )
            }

            // Loading Indicator
            if (uiState.isLoadingTasks || uiState.isUpdatingProject) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                )
            }
        }
    }

    // Edit Bottom Sheet
    if (uiState.showEditSheet) {
        EditProjectBottomSheet(
            initialTitle = uiState.title,
            initialDescription = uiState.description,
            onDismiss = { eventFlow.tryEmit(ProjectDetailEvent.DismissEditSheet) },
            onUpdate = { request ->
                eventFlow.tryEmit(ProjectDetailEvent.UpdateProject(request))
            }
        )
    }
}
