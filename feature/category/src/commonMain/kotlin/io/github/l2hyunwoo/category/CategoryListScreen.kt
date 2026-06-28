package io.github.l2hyunwoo.category

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.category.component.CategorySection
import io.github.l2hyunwoo.category.component.CreateCategoryBottomSheet
import io.github.l2hyunwoo.category.component.CreateProjectBottomSheet
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.core.design.component.moon.Moon
import io.github.l2hyunwoo.core.design.token.LUNAR_DURATION_STANDARD
import io.github.l2hyunwoo.core.design.token.LunarStandardEasing
import io.github.l2hyunwoo.data.categories.model.Category
import io.github.l2hyunwoo.data.categories.model.Project
import io.github.l2hyunwoo.kudos.core.common.compose.EventFlow
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableSharedFlow
import kudos.feature.category.generated.resources.Res
import kudos.feature.category.generated.resources.project_deleted
import kudos.feature.category.generated.resources.undo
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    uiState: CategoryListUiState,
    eventFlow: EventFlow<CategoryListEvent>,
    modifier: Modifier = Modifier,
    onNavigateToProjectDetail: (String, String, String, String?, String, String) -> Unit = { _, _, _, _, _, _ -> },
    // Top contentPadding so the first section clears the glass header owned by the parent
    // (MainScreen) OUTSIDE the backdrop recorder; the rest scrolls under it. Standalone usage
    // passes 0. The screen draws no top app bar of its own — the glass header carries the title.
    topContentPadding: Dp = 0.dp,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    var showCreateCategoryDialog by remember { mutableStateOf(false) }
    var showCreateProjectDialog by remember { mutableStateOf<String?>(null) }
    val snackbarMessage =
        uiState.deletedProject
            ?.let {
                stringResource(Res.string.project_deleted, it.project.title)
            }.orEmpty()
    val snackbarActionLabel = stringResource(Res.string.undo)

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error.message ?: "Unknown error occurred",
            )
        }
    }

    LaunchedEffect(uiState.deletedProject?.project?.id) {
        uiState.deletedProject?.let { deleted ->
            val result =
                snackbarHostState.showSnackbar(
                    message = snackbarMessage,
                    actionLabel = snackbarActionLabel,
                    duration = SnackbarDuration.Short,
                )

            when (result) {
                SnackbarResult.ActionPerformed -> {
                    eventFlow.tryEmit(
                        CategoryListEvent.UndoDeleteProject(
                            deleted.categoryId,
                            deleted.project,
                        ),
                    )
                }

                SnackbarResult.Dismissed -> {
                    // Snackbar dismissed: confirm deletion
                    eventFlow.tryEmit(
                        CategoryListEvent.ConfirmDeleteProject(
                            deleted.categoryId,
                            deleted.project.id,
                        ),
                    )
                }
            }
        }
    }

    Scaffold(
        // No top app bar: the parent's glass header carries the title. Transparent container so the
        // list is the only thing recorded into MainScreen's blur source (the screen background shows
        // through from the Scaffold behind it), and the rows scroll under the glass header.
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        modifier = modifier,
    ) { paddingValues ->
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
        ) {
            if (uiState.categories.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                    contentPadding =
                        PaddingValues(
                            // Clear the glass header (top) and the floating glass nav bar (bottom: 64dp bar +
                            // 32dp margin + system inset + 16dp), so the last section isn't hidden under it.
                            top = topContentPadding,
                            bottom =
                                64.dp + 32.dp +
                                    WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 16.dp,
                        ),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    uiState.categories.forEachIndexed { categoryIndex, category ->
                        stickyHeader(key = "header_${category.id}") {
                            // Filter out the deleted project if it belongs to this category
                            val filteredCategory =
                                if (uiState.deletedProject?.categoryId == category.id) {
                                    category.copy(
                                        projects =
                                            category.projects.filterNot {
                                                it.id == uiState.deletedProject.project.id
                                            },
                                    )
                                } else {
                                    category
                                }

                            CategorySection(
                                category = filteredCategory,
                                searchQuery = uiState.searchQuery,
                                onAddProjectClick = { showCreateProjectDialog = category.id },
                                onDeleteCategoryClick = {
                                    eventFlow.tryEmit(CategoryListEvent.DeleteCategory(category.id))
                                },
                                onProjectClick = { project ->
                                    onNavigateToProjectDetail(
                                        project.id,
                                        category.id,
                                        project.title,
                                        project.description,
                                        category.color,
                                        category.prefix,
                                    )
                                },
                                onDeleteProjectClick = { project ->
                                    eventFlow.tryEmit(
                                        CategoryListEvent.DeleteProject(
                                            category.id,
                                            project,
                                        ),
                                    )
                                },
                            )
                        }

                        if (categoryIndex < uiState.categories.lastIndex) {
                            item(key = "spacer_${category.id}") {
                                // When search filters whole categories out, the remaining sections reflow
                                // instead of snapping. The sticky header itself stays pinned (unanimated);
                                // animating the spacer is enough to smooth the gap collapse.
                                Spacer(
                                    modifier =
                                        Modifier
                                            .height(32.dp)
                                            .animateItem(
                                                fadeInSpec = KudosTheme.motion.standard,
                                                placementSpec =
                                                    tween<IntOffset>(
                                                        LUNAR_DURATION_STANDARD,
                                                        easing = LunarStandardEasing,
                                                    ),
                                                fadeOutSpec = KudosTheme.motion.micro,
                                            ),
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = KudosTheme.colors.brand.primary600,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        }
    }

    if (showCreateCategoryDialog) {
        CreateCategoryBottomSheet(
            onDismiss = { showCreateCategoryDialog = false },
            onCreate = { request ->
                eventFlow.tryEmit(CategoryListEvent.CreateCategory(request))
            },
        )
    }

    showCreateProjectDialog?.let { categoryId ->
        CreateProjectBottomSheet(
            categoryId = categoryId,
            onDismiss = { showCreateProjectDialog = null },
            onCreate = { catId, request ->
                eventFlow.tryEmit(CategoryListEvent.CreateProject(catId, request))
            },
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // New moon: nothing illuminated — the empty-list glyph (shared with TaskListScreen).
        Moon(k = 0f, size = 56.dp, modifier = Modifier.alpha(0.7f))
        Spacer(Modifier.height(16.dp))
        Text(
            text = "결과 없음",
            style = KudosTheme.typography.bodyLargeXB,
            color = KudosTheme.colors.ink.ink2,
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun CategoryListScreenPreview() {
    KudosTheme {
        CategoryListScreen(
            uiState =
                CategoryListUiState(
                    categories =
                        persistentListOf(
                            Category(
                                id = "1",
                                prefix = "WORK",
                                title = "Work Projects",
                                color = "#C9B8F0",
                                createdAt = "2024-01-01T00:00:00Z",
                                updatedAt = "2024-01-01T00:00:00Z",
                                projects =
                                    listOf(
                                        Project(
                                            id = "p1",
                                            title = "Mobile App Development",
                                            description = "Build a new mobile application",
                                            createdAt = "2024-01-01T00:00:00Z",
                                            updatedAt = "2024-01-01T00:00:00Z",
                                        ),
                                        Project(
                                            id = "p2",
                                            title = "API Integration",
                                            description = null,
                                            createdAt = "2024-01-02T00:00:00Z",
                                            updatedAt = "2024-01-02T00:00:00Z",
                                        ),
                                    ),
                            ),
                            Category(
                                id = "2",
                                prefix = "PERS",
                                title = "Personal",
                                color = "#A6E3C9",
                                createdAt = "2024-01-01T00:00:00Z",
                                updatedAt = "2024-01-01T00:00:00Z",
                                projects = emptyList(),
                            ),
                        ),
                    isLoading = false,
                    error = null,
                ),
            eventFlow = MutableSharedFlow(),
        )
    }
}
