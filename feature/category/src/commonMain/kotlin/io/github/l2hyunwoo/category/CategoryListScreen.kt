package io.github.l2hyunwoo.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.category.component.CategorySection
import io.github.l2hyunwoo.category.component.CreateCategoryDialog
import io.github.l2hyunwoo.category.component.CreateProjectDialog
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.data.categories.model.Category
import io.github.l2hyunwoo.data.categories.model.Project
import io.github.l2hyunwoo.kudos.core.common.compose.EventFlow
import io.github.l2hyunwoo.kudos.core.soil.appbar.AnimatedTextTopAppBar
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kudos.feature.category.generated.resources.Res
import kudos.feature.category.generated.resources.add_category
import kudos.feature.category.generated.resources.categories
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    uiState: CategoryListUiState,
    eventFlow: EventFlow<CategoryListEvent>,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var showCreateProjectDialog by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error.message ?: "Unknown error occurred"
            )
        }
    }

    Scaffold(
        topBar = {
            AnimatedTextTopAppBar(
                title = stringResource(Res.string.categories),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateCategoryDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.add_category)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                uiState.categories.forEachIndexed { categoryIndex, category ->
                    stickyHeader(key = "header_${category.id}") {
                        CategorySection(
                            category = category,
                            onAddProjectClick = { showCreateProjectDialog = category.id },
                            onDeleteCategoryClick = {
                                eventFlow.tryEmit(CategoryListEvent.DeleteCategory(category.id))
                            },
                            onDeleteProjectClick = { projectId ->
                                eventFlow.tryEmit(CategoryListEvent.DeleteProject(category.id, projectId))
                            }
                        )
                    }

                    if (categoryIndex < uiState.categories.lastIndex) {
                        item(key = "spacer_${category.id}") {
                            Spacer(modifier = Modifier.height(32.dp))
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    if (showCreateCategoryDialog) {
        CreateCategoryDialog(
            onDismiss = { showCreateCategoryDialog = false },
            onCreate = { request ->
                eventFlow.tryEmit(CategoryListEvent.CreateCategory(request))
            }
        )
    }

    showCreateProjectDialog?.let { categoryId ->
        CreateProjectDialog(
            categoryId = categoryId,
            onDismiss = { showCreateProjectDialog = null },
            onCreate = { catId, request ->
                eventFlow.tryEmit(CategoryListEvent.CreateProject(catId, request))
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryListScreenPreview() {
    KudosTheme {
        CategoryListScreen(
            uiState = CategoryListUiState(
                categories = persistentListOf(
                    Category(
                        id = "1",
                        prefix = "WORK",
                        title = "Work Projects",
                        color = "#FF6B6B",
                        createdAt = "2024-01-01T00:00:00Z",
                        updatedAt = "2024-01-01T00:00:00Z",
                        projects = listOf(
                            Project(
                                id = "p1",
                                title = "Mobile App Development",
                                description = "Build a new mobile application",
                                createdAt = "2024-01-01T00:00:00Z",
                                updatedAt = "2024-01-01T00:00:00Z"
                            ),
                            Project(
                                id = "p2",
                                title = "API Integration",
                                description = null,
                                createdAt = "2024-01-02T00:00:00Z",
                                updatedAt = "2024-01-02T00:00:00Z"
                            )
                        )
                    ),
                    Category(
                        id = "2",
                        prefix = "PERS",
                        title = "Personal",
                        color = "#4ECDC4",
                        createdAt = "2024-01-01T00:00:00Z",
                        updatedAt = "2024-01-01T00:00:00Z",
                        projects = emptyList()
                    )
                ),
                isLoading = false,
                error = null
            ),
            eventFlow = MutableSharedFlow<CategoryListEvent>()
        )
    }
}
