package ktorchat.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import ktorchat.common.*
import java.util.*

/**
 * Sends message from Client to Server.
 */
class MessageSender(private val serverHost: String) {
    private val client = HttpClient() {
        install(ContentNegotiation) {
            gson {
                serializeNulls()
            }
        }
        defaultRequest {
            url {
                this.host = serverHost
                this.port = Configuration.SERVER_PORT
            }
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun send(messageData: MessageData) {
        client.post("message") {
            setBody(messageData)
        }
    }

    suspend fun login(user: UserData): UUID? {
        val response: LoginResponse = client.post("login") {
            setBody(user)
        }.body()

        println(response.errorMessage ?: "Logged In! -> $response")
        return response.id
    }

    suspend fun logout(logoutData: LogoutData) {
        val response = client.post("logout") {
            setBody(logoutData)
        }

        println("Logged out! -> $response")
    }
}