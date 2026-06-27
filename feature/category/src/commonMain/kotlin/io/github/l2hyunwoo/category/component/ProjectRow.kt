package io.github.l2hyunwoo.category.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.data.categories.model.Project

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectRow(
    project: Project,
    categoryColor: String,
    onClick: () -> Unit = {},
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    searchQuery: String = ""
) {
    val dismissState = rememberSwipeToDismissBoxState()
    val rowShape = RoundedCornerShape(16.dp)
    val accent = Color(categoryColor.removePrefix("#").toLong(16) or 0xFF000000)

    // Resolve the periwinkle span color outside remember (theme read), then key on (title, query, color).
    val highlightColor = KudosTheme.colors.brand.primary600
    val titleText = remember(project.title, searchQuery, highlightColor) {
        highlighted(project.title, searchQuery, highlightColor)
    }

    SwipeToDismissBox(
        state = dismissState,
        // confirmValueChange is deprecated; the dismiss anchor set already excludes StartToEnd
        // (enableDismissFromStartToEnd = false), so onDismiss only fires for an EndToStart swipe.
        onDismiss = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
            }
        },
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = KudosTheme.colors.priority.urgent, shape = rowShape)
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color.White
                )
            }
        },
        enableDismissFromStartToEnd = false,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = KudosTheme.colors.surface.surface, shape = rowShape)
                .clip(rowShape)
                .clickable(onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accent)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titleText,
                    style = KudosTheme.typography.rowTitle,
                    // Base ink color for unmatched text; the highlight span overrides matched ranges.
                    color = KudosTheme.colors.ink.ink
                )

                project.description?.let { description ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = KudosTheme.typography.bodySmallR,
                        color = KudosTheme.colors.ink.ink2
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = KudosTheme.colors.ink.ink3
            )
        }
    }
}

// Tints every case-insensitive occurrence of [query] inside [text] with [highlightColor].
// Returns a plain AnnotatedString when there's nothing to highlight, so the no-search path renders
// byte-identically to the old plain-String Text. Pure (no Composable/theme reads) so the caller can
// remember it keyed on its inputs. Duplicated from TaskRow; a shared util for two call sites is overkill.
private fun highlighted(
    text: String,
    query: String,
    highlightColor: Color,
): AnnotatedString {
    val needle = query.trim()
    if (needle.isEmpty() || needle.length > text.length) return AnnotatedString(text)

    return buildAnnotatedString {
        var cursor = 0
        while (cursor <= text.length - needle.length) {
            val match = text.indexOf(needle, cursor, ignoreCase = true)
            if (match < 0) break
            append(text.substring(cursor, match))
            withStyle(SpanStyle(color = highlightColor)) {
                append(text.substring(match, match + needle.length))
            }
            cursor = match + needle.length
        }
        append(text.substring(cursor))
    }
}

// title "Mobile App Development"; query "app" tints the "App" occurrence periwinkle.
@Preview(showBackground = true)
@Composable
private fun ProjectRowHighlightPreview() {
    KudosTheme {
        ProjectRow(
            project = Project(
                id = "p1",
                title = "Mobile App Development",
                description = "Build a new mobile application",
                createdAt = "2024-01-01T00:00:00Z",
                updatedAt = "2024-01-01T00:00:00Z"
            ),
            categoryColor = "#C9B8F0",
            searchQuery = "app",
            onDelete = {},
        )
    }
}
