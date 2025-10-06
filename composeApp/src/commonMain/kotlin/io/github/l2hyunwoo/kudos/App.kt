package io.github.l2hyunwoo.kudos

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import io.github.l2hyunwoo.core.design.KudosTheme
import io.github.l2hyunwoo.kudos.navigation.taskListGraph
import org.jetbrains.compose.ui.tooling.preview.Preview
import soil.query.SwrCachePlus
import soil.query.SwrCacheScope
import soil.query.annotation.ExperimentalSoilQueryApi
import soil.query.compose.SwrClientProvider

@OptIn(ExperimentalSoilQueryApi::class)
@Composable
context(appGraph: AppGraph)
@Preview
fun App() {
    SwrClientProvider(SwrCachePlus(SwrCacheScope())) {
        KudosTheme {
            val navController = rememberNavController()
            Scaffold(
                modifier = Modifier.fillMaxSize()
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "main",
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    taskListGraph()
                }
            }
        }
    }
}