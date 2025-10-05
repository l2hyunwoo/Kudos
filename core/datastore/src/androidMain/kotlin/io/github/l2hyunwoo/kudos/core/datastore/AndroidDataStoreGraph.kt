package io.github.l2hyunwoo.kudos.core.datastore

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@ContributesTo(AppScope::class)
interface AndroidDataStoreGraph {
    @Provides
    fun provideDataStorePathProducer(context: Context): DataStorePathProducer {
        return DataStorePathProducer { fileName ->
            context.cacheDir.resolve(fileName).path
        }
    }
}
