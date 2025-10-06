package io.github.l2hyunwoo.data.categories.cache

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import dev.zacsweers.metro.Inject
import io.github.l2hyunwoo.data.categories.model.CategoriesResponse
import io.github.l2hyunwoo.data.categories.model.Category
import io.github.l2hyunwoo.kudos.core.datastore.annotation.CategoriesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Inject
class CategoriesCacheDataStore(
    @param:CategoriesDataStore private val dataStore: DataStore<Preferences>,
    private val json: Json,
) {
    suspend fun save(categories: CategoriesResponse) {
        dataStore.edit { preferences ->
            preferences[DATA_STORE_CATEGORIES_KEY] = json.encodeToString(
                ListSerializer(Category.serializer()),
                categories
            )
        }
    }

    suspend fun getCache(): CategoriesResponse? {
        return dataStore.data
            .map { preferences ->
                val serializedCache = preferences[DATA_STORE_CATEGORIES_KEY] ?: return@map null
                try {
                    json.decodeFromString(
                        ListSerializer(Category.serializer()),
                        serializedCache
                    )
                } catch (e: Throwable) {
                    e.printStackTrace()
                    null
                }
            }.firstOrNull()
    }

    fun getCacheSync(): CategoriesResponse? {
        return runBlocking { getCache() }
    }

    fun getCacheStream(): Flow<CategoriesResponse> {
        return dataStore.data.mapNotNull { preferences ->
            val serializedCache = preferences[DATA_STORE_CATEGORIES_KEY] ?: return@mapNotNull null
            json.decodeFromString(
                ListSerializer(Category.serializer()),
                serializedCache
            )
        }
    }

    private companion object {
        private val DATA_STORE_CATEGORIES_KEY = stringPreferencesKey("DATA_STORE_CATEGORIES_KEY")
    }
}
