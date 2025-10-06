package io.github.l2hyunwoo.kudos

import androidx.compose.ui.window.ComposeUIViewController

fun MainViewController(appGraph: AppGraph) = ComposeUIViewController {
    with(appGraph) { App() }
}