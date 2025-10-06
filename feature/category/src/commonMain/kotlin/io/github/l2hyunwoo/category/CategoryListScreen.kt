package io.github.l2hyunwoo.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.category.component.CategorySection
import io.github.l2hyunwoo.data.categories.model.Category
import io.github.l2hyunwoo.kudos.core.soil.appbar.AnimatedTextTopAppBar
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryListScreen(
    categories: ImmutableList<Category>,
    onAddProjectClick: (String) -> Unit = {},
    onDeleteCategoryClick: (String) -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        topBar = {
            AnimatedTextTopAppBar(
                title = "Categories",
                scrollBehavior = scrollBehavior
            )
        },
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            categories.forEachIndexed { categoryIndex, category ->
                stickyHeader(key = "header_${category.id}") {
                    CategorySection(
                        category = category,
                        onAddProjectClick = onAddProjectClick
                    )
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
