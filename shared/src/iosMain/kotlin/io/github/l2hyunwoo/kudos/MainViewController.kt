package io.github.l2hyunwoo.kudos

import androidx.compose.ui.window.ComposeUIViewController
import dev.zacsweers.metro.createGraphFactory

// Swift-callable entry point. createGraphFactory is a reified inline function, so the
// graph must be built here in Kotlin rather than from Swift.
fun MainViewController() = ComposeUIViewController {
    val appGraph = createGraphFactory<IosAppGraph.Factory>().createIosAppGraph()
    with(appGraph) { App() }
}