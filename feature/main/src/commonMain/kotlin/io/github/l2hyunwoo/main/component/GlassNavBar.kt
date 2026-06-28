package io.github.l2hyunwoo.main.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ListAlt
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.skydoves.cloudy.Sky
import io.github.l2hyunwoo.core.design.component.surface.glassSurface
import io.github.l2hyunwoo.main.MainTab
import kudos.feature.main.generated.resources.Res
import kudos.feature.main.generated.resources.dashboard
import kudos.feature.main.generated.resources.tasks
import org.jetbrains.compose.resources.stringResource

@Composable
fun GlassNavBar(
    selectedTab: MainTab,
    onSelectTab: (MainTab) -> Unit,
    onAdd: () -> Unit,
    sky: Sky,
    modifier: Modifier = Modifier,
) {
    Box(modifier.fillMaxWidth().height(64.dp)) {
        // Background: blurred fill + shadow/clip/border, no children (background-only glassSurface).
        Box(
            Modifier
                .fillMaxSize()
                .glassSurface(sky = sky, shape = PillShape),
        )
        // Foreground sibling: the nav items, overlaid on (not children of) the glass background, so
        // none of their pixels fold back into the blur source.
        Row(
            Modifier
                .fillMaxSize()
                // Inner inset so a selected item's pill never reaches the glass bar's rounded edge;
                // without it the parent pill clip shears the item-pill's outer corner ("squished").
                .padding(horizontal = 6.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            NavBarItem(
                icon = Icons.AutoMirrored.Rounded.ListAlt,
                label = stringResource(Res.string.tasks),
                selected = selectedTab == MainTab.TASKS,
                onClick = { onSelectTab(MainTab.TASKS) },
                modifier = Modifier.weight(1f),
            )
            CenterAddButton(
                onClick = onAdd,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            NavBarItem(
                icon = Icons.Rounded.BarChart,
                label = stringResource(Res.string.dashboard),
                selected = selectedTab == MainTab.DASHBOARD,
                onClick = { onSelectTab(MainTab.DASHBOARD) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}
