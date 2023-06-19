package ktorchat.client

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import ktorchat.common.Configuration
import ktorchat.common.MessageData
import ktorchat.common.UserData

/**
 * Sends message from Client to Server.
 */
class MessageSender() {
    private val client = HttpClient() {
        install(ContentNegotiation) {
            json()
        }
        defaultRequest {
            url {
                protocol = URLProtocol.HTTP
                this.host = Configuration.SERVER_HOST
                this.port = Configuration.SERVER_PORT
            }
            contentType(ContentType.Application.Json)
        }
    }

    suspend fun send(messageData: MessageData) {
        client.post("message/") {
            setBody(messageData)
        }
    }

    suspend fun login(user: UserData) {
        client.post("login/") {
            setBody(user)
        }
    }
}