package io.github.l2hyunwoo.kudos.core.datastore

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.l2hyunwoo.kudos.core.datastore.annotation.IoDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@ContributesTo(AppScope::class)
interface CoroutinesDispatchersProviders {
    @Provides
    @IoDispatchers
    fun provideIoDispatchers(): CoroutineDispatcher = Dispatchers.IO
}
