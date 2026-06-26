package io.github.l2hyunwoo.kudos.core.network

import Kudos.core.network.BuildConfig
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.l2hyunwoo.kudos.core.common.DataScope
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
import kotlinx.serialization.json.Json

@ContributesTo(DataScope::class)
interface IosNetworkGraph {

    @Provides
    @SingleIn(DataScope::class)
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
}
