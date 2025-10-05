package io.github.l2hyunwoo.kudos

import Kudos.composeApp.BuildConfig
import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.l2hyunwoo.data.tasks.api.DefaultTasksApiClient
import io.github.l2hyunwoo.data.tasks.api.TasksApiClient
import io.github.l2hyunwoo.kudos.core.datastore.DataStorePathProducer
import io.github.l2hyunwoo.kudos.core.network.di.NetworkScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ExportObjCClass
import kotlinx.serialization.json.Json
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

/**
 * The iOS dependency graph cannot currently be resolved by the compiler plugin.
 * Therefore, we need to define the iOS dependency graph manually.
 * For more details, see: https://github.com/ZacSweers/metro/issues/460
 */
@OptIn(BetaInteropApi::class)
@ExportObjCClass
@DependencyGraph(
    scope = AppScope::class,
    additionalScopes = [NetworkScope::class],
)
interface IosAppGraph : AppGraph {

    @Binds
    val DefaultTasksApiClient.bind: TasksApiClient

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
}
