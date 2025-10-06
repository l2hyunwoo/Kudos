package io.github.l2hyunwoo.kudos

import Kudos.composeApp.BuildConfig
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.l2hyunwoo.data.tasks.api.DefaultTasksApiClient
import io.github.l2hyunwoo.data.tasks.api.TasksApiClient
import io.github.l2hyunwoo.data.tasks.model.TasksResponse
import io.github.l2hyunwoo.data.tasks.query.DefaultTasksQueryKey
import io.github.l2hyunwoo.kudos.core.common.DataScope
import io.github.l2hyunwoo.kudos.core.datastore.DataStorePathProducer
import io.github.l2hyunwoo.kudos.core.datastore.DataStoreProviders.Companion.DATA_STORE_TASKS_FILE_NAME
import io.github.l2hyunwoo.kudos.core.datastore.annotation.IoDispatchers
import io.github.l2hyunwoo.kudos.core.datastore.annotation.TasksDataStore
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ExportObjCClass
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask
import soil.query.QueryKey

/**
 * The iOS dependency graph cannot currently be resolved by the compiler plugin.
 * Therefore, we need to define the iOS dependency graph manually.
 * For more details, see: https://github.com/ZacSweers/metro/issues/460
 */
@OptIn(BetaInteropApi::class)
@ExportObjCClass
@DependencyGraph(
    scope = AppScope::class,
    additionalScopes = [DataScope::class],
)
interface IosAppGraph : AppGraph {

    @Binds
    val DefaultTasksApiClient.bind: TasksApiClient

    @Binds
    val DefaultTasksQueryKey.bind: QueryKey<List<TasksResponse.CategoryWithTasks>>

    @Provides
    @SingleIn(AppScope::class)
    fun provideJson(): Json = Json {
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        prettyPrint = true
    }

    @Provides
    @SingleIn(AppScope::class)
    fun provideHttpClient(json: Json): HttpClient = HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(json)
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
        }
        defaultRequest {
            header("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
        }
    }

    @Provides
    @SingleIn(AppScope::class)
    fun provideKtorfit(
        httpClient: HttpClient
    ) = Ktorfit.Builder()
        .httpClient(httpClient)
        .baseUrl(BuildConfig.SUPABASE_URL)
        .build()

    @OptIn(ExperimentalForeignApi::class)
    @Provides
    fun providesDataStorePathProducer(): DataStorePathProducer {
        return DataStorePathProducer { fileName ->
            val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null,
            )
            requireNotNull(documentDirectory).path + "/$fileName"
        }
    }

    @IoDispatchers
    @Provides
    fun provideIoDispatcher(): CoroutineDispatcher {
        // Since Kotlin/Native doesn't support Dispatchers.IO, we use Dispatchers.Default instead.
        return Dispatchers.Default
    }

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

    @DependencyGraph.Factory
    fun interface Factory {
        fun createIosAppGraph(): IosAppGraph
    }
}
