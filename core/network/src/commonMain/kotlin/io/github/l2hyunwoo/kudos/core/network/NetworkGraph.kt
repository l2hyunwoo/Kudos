package io.github.l2hyunwoo.kudos.core.network

import Kudos.core.network.BuildConfig
import de.jensklingenberg.ktorfit.Ktorfit
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.l2hyunwoo.kudos.core.common.DataScope
import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

@ContributesTo(DataScope::class)
interface NetworkGraph {

    companion object {
        @Provides
        @SingleIn(DataScope::class)
        fun provideJson(): Json = Json {
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
            // Omit null-valued properties on encode so partial-update payloads
            // (UpdateTaskRequest) only carry the fields actually being changed.
            // Safe for decode: response models give nullable fields a `= null` default.
            explicitNulls = false
            prettyPrint = true
        }

        @Provides
        @SingleIn(DataScope::class)
        fun provideKtorfit(
            httpClient: HttpClient
        ): Ktorfit = Ktorfit.Builder()
            .httpClient(httpClient)
            .baseUrl(BuildConfig.SUPABASE_URL)
            .build()
    }
}
