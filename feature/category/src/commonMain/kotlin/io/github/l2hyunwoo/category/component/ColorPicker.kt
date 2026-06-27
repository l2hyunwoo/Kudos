package io.github.l2hyunwoo.category.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.data.categories.model.CategoryColor
import kudos.feature.category.generated.resources.Res
import kudos.feature.category.generated.resources.select_color
import org.jetbrains.compose.resources.stringResource

@Composable
fun ColorPicker(
    selectedColor: CategoryColor?,
    onColorSelected: (CategoryColor) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(Res.string.select_color).uppercase(),
            style = KudosTheme.typography.eyebrow,
            color = KudosTheme.colors.ink.ink2
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(CategoryColor.entries, key = { it.name }) { color ->
                ColorSwatch(
                    color = color,
                    isSelected = selectedColor == color,
                    onClick = { onColorSelected(color) }
                )
            }
        }
    }
}

@Composable
private fun ColorSwatch(
    color: CategoryColor,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pastel = Color(color.hexCode.removePrefix("#").toLong(16) or 0xFF000000)
    // Check icon and selected ring read against the light pastel fill: ink for the glyph,
    // periwinkle for the ring.
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(pastel)
            .then(
                if (isSelected) {
                    Modifier.border(3.dp, KudosTheme.colors.brand.primary600, CircleShape)
                        .padding(3.dp)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = KudosTheme.colors.ink.ink,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
