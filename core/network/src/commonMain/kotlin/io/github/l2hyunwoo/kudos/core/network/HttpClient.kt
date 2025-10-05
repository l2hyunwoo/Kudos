package io.github.l2hyunwoo.kudos.core.network

import io.ktor.client.HttpClient
import kotlinx.serialization.json.Json

expect fun httpClient(json: Json): HttpClient
