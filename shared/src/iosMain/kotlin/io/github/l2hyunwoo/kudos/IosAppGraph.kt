package io.github.l2hyunwoo.kudos

// BuildConfig is generated into this same package (io.github.l2hyunwoo.kudos) via the
// buildConfig { packageName(...) } pin in build.gradle.kts, so no import is required.
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Binds
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.l2hyunwoo.data.categories.api.CategoriesApiClient
import io.github.l2hyunwoo.data.categories.api.DefaultCategoriesApiClient
import io.github.l2hyunwoo.data.categories.model.CategoriesSubscriptionKey
import io.github.l2hyunwoo.data.categories.model.Category
import io.github.l2hyunwoo.data.categories.model.CreateCategoryMutationKey
import io.github.l2hyunwoo.data.categories.model.CreateProjectMutationKey
import io.github.l2hyunwoo.data.categories.model.DeleteCategoryMutationKey
import io.github.l2hyunwoo.data.categories.mutation.DefaultCreateCategoryMutationKey
import io.github.l2hyunwoo.data.categories.mutation.DefaultCreateProjectMutationKey
import io.github.l2hyunwoo.data.categories.mutation.DefaultDeleteCategoryMutationKey
import io.github.l2hyunwoo.data.categories.query.DefaultCategoriesQueryKey
import io.github.l2hyunwoo.data.categories.subscription.DefaultCategoriesSubscriptionKey
import io.github.l2hyunwoo.data.tasks.api.DefaultTasksApiClient
import io.github.l2hyunwoo.data.tasks.api.TasksApiClient
import io.github.l2hyunwoo.data.tasks.model.TasksResponse
import io.github.l2hyunwoo.data.tasks.query.DefaultTasksQueryKey
import io.github.l2hyunwoo.kudos.core.common.DataScope
import io.github.l2hyunwoo.kudos.core.datastore.DataStorePathProducer
import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ExportObjCClass
import kotlinx.serialization.json.Json
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
    val DefaultCategoriesApiClient.bind: CategoriesApiClient

    @Binds
    val DefaultCategoriesQueryKey.bind: QueryKey<List<Category>>

    @Binds
    val DefaultCategoriesSubscriptionKey.bind: CategoriesSubscriptionKey

    @Binds
    val DefaultCreateCategoryMutationKey.bind: CreateCategoryMutationKey

    @Binds
    val DefaultDeleteCategoryMutationKey.bind: DeleteCategoryMutationKey

    @Binds
    val DefaultCreateProjectMutationKey.bind: CreateProjectMutationKey

    @Binds
    val DefaultTasksApiClient.bind: TasksApiClient

    @Binds
    val DefaultTasksQueryKey.bind: QueryKey<List<TasksResponse.CategoryWithTasks>>

    // iOS-only: the Ktor engine is platform-specific. commonMain NetworkGraph provides
    // Json/Ktorfit (engine-agnostic) and androidMain AndroidNetworkGraph supplies the OkHttp
    // engine; here we supply the Darwin engine. Json, Ktorfit and @IoDispatchers are NOT
    // declared in this graph because the commonMain @ContributesTo(DataScope) providers
    // (NetworkGraph, CoroutinesDispatchersProviders) already bind them for the iOS target.
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
            header(HttpHeaders.ContentType, ContentType.Application.Json)
        }
    }

    // iOS-only: resolves the DataStore file path via NSFileManager. commonMain
    // DataStoreProviders consumes this producer to build the actual DataStore instances,
    // so this graph only injects the platform path producer (mirrors AndroidAppGraph, which
    // contributes only its platform-specific Context).
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

    @DependencyGraph.Factory
    fun interface Factory {
        fun createIosAppGraph(): IosAppGraph
    }
}
