package io.github.l2hyunwoo.kudos

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.core.design.transition.LocalSharedTransitionScope
import io.github.l2hyunwoo.kudos.core.common.navigation.Main
import io.github.l2hyunwoo.kudos.navigation.categoryListGraph
import io.github.l2hyunwoo.kudos.navigation.mainScreenGraph
import io.github.l2hyunwoo.kudos.navigation.projectDetailGraph
import io.github.l2hyunwoo.kudos.navigation.taskDetailGraph
import io.github.l2hyunwoo.kudos.navigation.taskListGraph
import androidx.compose.ui.tooling.preview.Preview
import soil.query.SwrCachePlus
import soil.query.SwrCacheScope
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.compose.SwrClientProvider

@OptIn(ExperimentalSoilQueryApi::class, ExperimentalSharedTransitionApi::class)
@Composable
context(appGraph: AppGraph)
@Preview
fun App() {
    SwrClientProvider(SwrCachePlus(SwrCacheScope())) {
        // null = follow the system setting; non-null = user override. Survives config change via
        // rememberSaveable (in-memory across recreation is enough; no persistence requirement).
        var userDark: Boolean? by rememberSaveable { mutableStateOf(null) }
        val dark = userDark ?: isSystemInDarkTheme()
        KudosTheme(darkTheme = dark) {
            val navController = rememberNavController()
            Scaffold(
                modifier = Modifier.fillMaxSize()
            ) {
                // SharedTransitionLayout is `this: SharedTransitionScope`. Expose it via the local so
                // leaf composables in the row and the detail screen can opt into shared elements; each
                // destination additionally provides its own AnimatedContentScope (see the graphs).
                SharedTransitionLayout {
                    CompositionLocalProvider(LocalSharedTransitionScope provides this) {
                        NavHost(
                            navController = navController,
                            startDestination = Main,
                            modifier = Modifier
                                .fillMaxSize()
                                .imePadding()
                                .background(MaterialTheme.colorScheme.background)
                        ) {
                            mainScreenGraph(
                                navController = navController,
                                darkTheme = dark,
                                onToggleTheme = { userDark = !dark },
                            )
                            taskListGraph(navController)
                            categoryListGraph(navController)
                            projectDetailGraph(navController)
                            taskDetailGraph(navController)
                        }
                    }
                }
            }
        }
    }
}