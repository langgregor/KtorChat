package ktorchat.client

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import ktorchat.common.Configuration
import ktorchat.common.LoginResponse
import ktorchat.common.MessageData
import ktorchat.common.UserData
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
        val response = client.post("message") {
            setBody(messageData)
        }
        println("Sent message! -> $response")
    }

    suspend fun login(user: UserData): UUID? {
        val response: LoginResponse = client.post("login") {
            setBody(user)
        }.body()

        println(response.errorMessage ?: "Logged In! -> $response")
        return response.id
    }
}