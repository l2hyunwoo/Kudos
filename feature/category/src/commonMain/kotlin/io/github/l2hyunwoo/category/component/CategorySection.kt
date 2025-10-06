package io.github.l2hyunwoo.category.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.data.categories.model.Category
import kudos.feature.category.generated.resources.Res
import kudos.feature.category.generated.resources.add_project
import kudos.feature.category.generated.resources.cancel
import kudos.feature.category.generated.resources.delete
import kudos.feature.category.generated.resources.delete_category
import kudos.feature.category.generated.resources.delete_category_confirmation
import kudos.feature.category.generated.resources.no_projects_yet
import org.jetbrains.compose.resources.stringResource

@Composable
fun CategorySection(
    category: Category,
    onAddProjectClick: () -> Unit,
    onDeleteCategoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        // Category Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Category Prefix Badge
                Text(
                    text = category.prefix,
                    style = KudosTheme.typography.labelSmallM,
                    color = Color.White,
                    modifier = Modifier
                        .background(
                            color = Color(android.graphics.Color.parseColor(category.color)),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .clip(RoundedCornerShape(4.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                Text(
                    text = category.title.uppercase(),
                    style = KudosTheme.typography.labelLargeM,
                    color = KudosTheme.colorScheme.secondary,
                )
            }

            Row {
                IconButton(onClick = onAddProjectClick) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(Res.string.add_project),
                        tint = KudosTheme.colorScheme.primary
                    )
                }

                IconButton(onClick = { showDeleteConfirmation = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.delete_category),
                        tint = KudosTheme.colorScheme.error
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Projects List
        if (category.projects.isEmpty()) {
            Text(
                text = stringResource(Res.string.no_projects_yet),
                style = KudosTheme.typography.bodySmallR,
                color = KudosTheme.colorScheme.tertiary,
                modifier = Modifier.padding(start = 16.dp)
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                category.projects.forEach { project ->
                    ProjectRow(project = project)
                }
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(Res.string.delete_category)) },
            text = { Text(stringResource(Res.string.delete_category_confirmation)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteCategoryClick()
                        showDeleteConfirmation = false
                    }
                ) {
                    Text(stringResource(Res.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text(stringResource(Res.string.cancel))
                }
            }
        )
    }
}
