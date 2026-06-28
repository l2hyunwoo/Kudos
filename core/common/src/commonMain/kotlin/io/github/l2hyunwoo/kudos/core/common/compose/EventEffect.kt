package io.github.l2hyunwoo.kudos.core.common.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

typealias EventFlow<T> = MutableSharedFlow<T>

@Composable
fun <T> rememberEventFlow(): EventFlow<T> =
    remember {
        MutableSharedFlow(extraBufferCapacity = 20)
    }

@Composable
fun <EVENT> EventEffect(
    eventFlow: EventFlow<EVENT>,
    block: suspend CoroutineScope.(event: EVENT) -> Unit,
) {
    val currentBlock by rememberUpdatedState(block)
    LaunchedEffect(eventFlow) {
        supervisorScope {
            eventFlow.collect { event ->
                launch {
                    currentBlock(event)
                }
            }
        }
    }
}
