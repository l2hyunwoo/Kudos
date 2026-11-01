package io.github.l2hyunwoo.kudos.navigation

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.Composition
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Recomposer
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.retain.RetainObserver
import androidx.compose.runtime.retain.retain
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

class RetainedEntryContentTest {
    @Test
    fun retainsContextAcrossCompositionRecreationUntilEntryIsRemoved() {
        val entry = Entry()
        val first = composeEntry(entry)
        val context = first.context
        first.dispose()
        assertFalse(context.retired)

        val restored = composeEntry(entry)
        assertSame(context, restored.context)
        restored.dispose()
        entry.viewModelStore.clear()
        assertTrue(context.retired)
        assertEquals(1, context.retireCount)
    }

    @Test
    fun keepsEntriesIndependentAndCreatesNewContextAfterPop() {
        val firstEntry = Entry()
        val secondEntry = Entry()
        val first = composeEntry(firstEntry)
        first.dispose()
        val second = composeEntry(secondEntry)
        assertNotSame(first.context, second.context)
        second.dispose()
        secondEntry.viewModelStore.clear()
        assertTrue(second.context.retired)
        assertFalse(first.context.retired)

        val returned = composeEntry(firstEntry)
        assertSame(first.context, returned.context)
        returned.dispose()
        firstEntry.viewModelStore.clear()

        val newEntry = Entry()
        val fresh = composeEntry(newEntry)
        assertNotSame(first.context, fresh.context)
        fresh.dispose()
        newEntry.viewModelStore.clear()
    }

    private fun composeEntry(entry: Entry): ComposedEntry {
        val recomposer = Recomposer(EmptyCoroutineContext)
        val composition = Composition(EmptyApplier(), recomposer)
        lateinit var context: Context
        composition.setContent {
            CompositionLocalProvider(LocalViewModelStoreOwner provides entry) {
                RetainedEntryContent {
                    val retained = retain { Context() }
                    SideEffect { context = retained }
                }
            }
        }
        return ComposedEntry(context, composition, recomposer)
    }

    private class Entry : ViewModelStoreOwner {
        override val viewModelStore = ViewModelStore()
    }

    private class Context : RetainObserver {
        var retireCount = 0
        val retired get() = retireCount > 0

        override fun onRetained() = Unit

        override fun onEnteredComposition() = Unit

        override fun onExitedComposition() = Unit

        override fun onUnused() = Unit

        override fun onRetired() {
            retireCount++
        }
    }

    private class ComposedEntry(
        val context: Context,
        val composition: Composition,
        val recomposer: Recomposer,
    ) {
        fun dispose() {
            composition.dispose()
            recomposer.cancel()
        }
    }

    private class EmptyApplier : AbstractApplier<Unit>(Unit) {
        override fun insertTopDown(
            index: Int,
            instance: Unit,
        ) = Unit

        override fun insertBottomUp(
            index: Int,
            instance: Unit,
        ) = Unit

        override fun remove(
            index: Int,
            count: Int,
        ) = Unit

        override fun move(
            from: Int,
            to: Int,
            count: Int,
        ) = Unit

        override fun onClear() = Unit
    }
}
