package io.github.l2hyunwoo.tasks

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.data.tasks.model.TasksResponse
import io.github.l2hyunwoo.data.tasks.model.fixture
import io.github.l2hyunwoo.kudos.core.soil.appbar.AnimatedTextTopAppBar
import io.github.l2hyunwoo.tasks.component.TaskRow
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kudos.feature.tasks.generated.resources.Res
import kudos.feature.tasks.generated.resources.tasks
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    categories: ImmutableList<TasksResponse.CategoryWithTasks>,
    onEditCategoriesClick: () -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        topBar = {
            AnimatedTextTopAppBar(
                title = stringResource(Res.string.tasks),
                scrollBehavior = scrollBehavior,
                actions = {
                    IconButton(onClick = onEditCategoriesClick) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit categories"
                        )
                    }
                }
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier.padding(it)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            categories.forEachIndexed { categoryIndex, category ->
                stickyHeader(key = "header_${category.id}") {
                    Text(
                        text = category.title.uppercase(),
                        style = KudosTheme.typography.labelLargeM,
                        color = KudosTheme.colorScheme.secondary,
                    )
                }

                items(
                    count = category.tasks.size,
                    key = { taskIndex -> category.tasks[taskIndex].id }
                ) { taskIndex ->
                    val task = category.tasks[taskIndex]
                    TaskRow(task = task)
                }

                // 마지막 카테고리가 아니면 spacer 추가
                if (categoryIndex < categories.lastIndex) {
                    item(key = "spacer_${category.id}") {
                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TaskListScreenPreview() {
    KudosTheme { TaskListScreen(persistentListOf(TasksResponse.CategoryWithTasks.fixture)) }
}
