package io.github.l2hyunwoo.kudos.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.l2hyunwoo.kudos.core.common.DataScope
import io.github.l2hyunwoo.kudos.core.datastore.annotation.IoDispatchers
import io.github.l2hyunwoo.kudos.core.datastore.annotation.TasksDataStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import okio.Path.Companion.toPath

@ContributesTo(DataScope::class)
interface DataStoreProviders {

    @SingleIn(DataScope::class)
    @TasksDataStore
    @Provides
    fun provideTasksDataStore(
        dataStorePathProducer: DataStorePathProducer,
        @IoDispatchers ioDispatcher: CoroutineDispatcher,
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.createWithPath(
            corruptionHandler = ReplaceFileCorruptionHandler({ emptyPreferences() }),
            migrations = emptyList(),
            scope = CoroutineScope(ioDispatcher),
            produceFile = {
                dataStorePathProducer.producePath(DATA_STORE_TASKS_FILE_NAME).toPath()
            },
        )
    }

    companion object {
        const val DATA_STORE_TASKS_FILE_NAME: String = "kudos.tasks.preferences_pb"
    }
}
