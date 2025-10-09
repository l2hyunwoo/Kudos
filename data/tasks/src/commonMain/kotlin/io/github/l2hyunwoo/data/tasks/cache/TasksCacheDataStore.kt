package io.github.l2hyunwoo.data.tasks.cache

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.Inject
import io.github.l2hyunwoo.data.tasks.model.TasksResponse
import io.github.l2hyunwoo.kudos.core.datastore.annotation.TasksDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

@Inject
class TasksCacheDataStore(
    @param:TasksDataStore private val dataStore: DataStore<Preferences>,
    private val json: Json,
) {
    suspend fun save(tasks: TasksResponse) {
        dataStore.edit { preferences ->
            preferences[DATA_STORE_TASKS_KEY] = json.encodeToString(tasks)
        }
    }

    suspend fun getCache(): TasksResponse? {
        return dataStore.data
            .map { preferences ->
                val serializedCache = preferences[DATA_STORE_TASKS_KEY] ?: return@map null
                try {
                    json.decodeFromString(TasksResponse.serializer(), serializedCache)
                } catch (e: Throwable) {
                    e.printStackTrace()
                    null
                }
            }.firstOrNull()
    }

    fun getCacheSync(): TasksResponse? {
        return runBlocking { getCache() }
    }

    fun getCacheStream(): Flow<TasksResponse> {
        return dataStore.data.mapNotNull { preferences ->
            val serializedCache = preferences[DATA_STORE_TASKS_KEY] ?: return@mapNotNull null
            json.decodeFromString(TasksResponse.serializer(), serializedCache)
        }
    }

    suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(DATA_STORE_TASKS_KEY)
        }
    }

    private companion object {
        private val DATA_STORE_TASKS_KEY = stringPreferencesKey("DATA_STORE_TASKS_KEY")
    }
}
