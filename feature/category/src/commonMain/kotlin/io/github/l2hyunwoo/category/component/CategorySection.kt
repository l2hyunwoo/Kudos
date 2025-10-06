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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.data.categories.model.Category

@Composable
fun CategorySection(
    category: Category,
    onAddProjectClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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

            IconButton(onClick = { onAddProjectClick(category.id) }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Project",
                    tint = KudosTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Projects List
        if (category.projects.isEmpty()) {
            Text(
                text = "No projects yet",
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
}
