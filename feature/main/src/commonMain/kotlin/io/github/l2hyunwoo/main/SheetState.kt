package io.github.l2hyunwoo.main

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

// A single nullable enum keeps the two create-sheets mutually exclusive by construction.
enum class ActiveSheet {
    CREATE_TASK,
    CREATE_CATEGORY,
}

@Stable
class SheetState(
    active: ActiveSheet? = null,
) {
    var active by mutableStateOf(active)

    fun showCreateTask() {
        active = ActiveSheet.CREATE_TASK
    }

    fun showCreateCategory() {
        active = ActiveSheet.CREATE_CATEGORY
    }

    fun dismiss() {
        active = null
    }

    companion object {
        // null element = no sheet open; the ?.let guards valueOf against it.
        val Saver: Saver<SheetState, Any> =
            listSaver(
                save = { listOf(it.active?.name) },
                restore = { SheetState(it[0]?.let(ActiveSheet::valueOf)) },
            )
    }
}

@Composable
fun rememberSheetState(): SheetState = rememberSaveable(saver = SheetState.Saver) { SheetState() }
