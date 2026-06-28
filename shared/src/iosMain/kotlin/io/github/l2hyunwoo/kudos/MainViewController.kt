package io.github.l2hyunwoo.kudos

import androidx.compose.ui.window.ComposeUIViewController
import dev.zacsweers.metro.createGraphFactory

// Swift-callable entry point. createGraphFactory is a reified inline function, so the
// graph must be built here in Kotlin rather than from Swift. PascalCase is the iOS factory
// convention this is called by from Swift, so the standard lowercase rule does not apply.
@Suppress("ktlint:standard:function-naming")
fun MainViewController() =
    ComposeUIViewController {
        val appGraph = createGraphFactory<IosAppGraph.Factory>().createIosAppGraph()
        with(appGraph) { App() }
    }
