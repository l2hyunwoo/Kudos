package io.github.l2hyunwoo.kudos.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.retain.RetainedValuesStoreRegistry
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
internal fun RetainedEntryContent(
    owner: EntryRetainedValues = viewModel { EntryRetainedValues() },
    content: @Composable () -> Unit,
) {
    // Navigation 2 does not provide a retain store; its entry ViewModel owns the back stack lifetime.
    owner.registry.LocalRetainedValuesStoreProvider(Unit, content)
}

internal class EntryRetainedValues : ViewModel() {
    val registry = RetainedValuesStoreRegistry()

    override fun onCleared() {
        registry.dispose()
    }
}
