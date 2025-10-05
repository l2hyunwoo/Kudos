package io.github.l2hyunwoo.kudos.core.network

import Kudos.core.network.BuildConfig
import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.l2hyunwoo.kudos.core.network.di.NetworkScope
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

@ContributesTo(NetworkScope::class)
interface NetworkGraph {

    companion object {
        @Provides
        @SingleIn(NetworkScope::class)
        fun provideJson(): Json = Json {
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
            prettyPrint = true
        }

        @Provides
        @SingleIn(NetworkScope::class)
        fun provideHttpClient(json: Json): HttpClient = httpClient(json)

        @Provides
        @SingleIn(NetworkScope::class)
        fun provideKtorfit(
            httpClient: HttpClient
        ) = Ktorfit.Builder()
            .httpClient(httpClient)
            .baseUrl(BuildConfig.SUPABASE_URL)
            .build()
    }
}
