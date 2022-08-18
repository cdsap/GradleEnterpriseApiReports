package io.github.cdsap.geapi.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.serialization.gson.gson

class GEClient(private val token: String, geServer: String) {
    val client = createHttpClient()
    val url = "$geServer/api/builds"

    private fun createHttpClient() = HttpClient(CIO) {
        install(ContentNegotiation) {
            gson()
        }
        install(Auth) {
            bearer {
                loadTokens {
                    BearerTokens(token, "")
                }
            }
        }
    }

    suspend inline fun <reified T : Any> get(url: String): T {
        return client.get(url).body()
    }
}
