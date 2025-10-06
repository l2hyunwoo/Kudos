package io.github.l2hyunwoo.kudos.core.network

import Kudos.core.network.BuildConfig
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.l2hyunwoo.kudos.core.common.DataScope
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import okhttp3.logging.HttpLoggingInterceptor

@ContributesTo(DataScope::class)
interface AndroidNetworkGraph {

    @Provides
    @SingleIn(DataScope::class)
    fun provideHttpClient(json: Json): HttpClient = HttpClient(OkHttp) {
        engine {
            config {
                addInterceptor(
                    HttpLoggingInterceptor().apply {
                        level = HttpLoggingInterceptor.Level.BODY
                    }
                )
            }
        }
        install(ContentNegotiation) {
            json(json)
        }
        defaultRequest {
            header("Authorization", "Bearer ${BuildConfig.SUPABASE_ANON_KEY}")
        }
    }
}
