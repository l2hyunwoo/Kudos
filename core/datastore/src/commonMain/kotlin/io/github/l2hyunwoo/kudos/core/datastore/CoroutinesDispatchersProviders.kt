package io.github.l2hyunwoo.kudos.core.datastore

import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.github.l2hyunwoo.kudos.core.common.DataScope
import io.github.l2hyunwoo.kudos.core.datastore.annotation.IoDispatchers
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@ContributesTo(DataScope::class)
interface CoroutinesDispatchersProviders {
    @Provides
    @IoDispatchers
    fun provideIoDispatchers(): CoroutineDispatcher = Dispatchers.IO
}
