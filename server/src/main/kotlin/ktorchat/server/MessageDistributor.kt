package ktorchat.server

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.gson.*
import ktorchat.common.Configuration
import ktorchat.common.MessageData
import java.util.*

class MessageDistributor {
    private var client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            gson {
                serializeNulls()
            }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 1000
        }
    }

    suspend fun distribute(message: MessageData, userIdToHostResolver: (UUID) -> String?) {
        message.targetUsers.mapNotNull(userIdToHostResolver).forEach { host ->
            val response = client.post("receive") {
                url {
                    this.host = host
                    port = Configuration.CLIENT_PORT
                }
                setBody(message)
            }
            println("Distributed message from ${userIdToHostResolver(message.sourceUser)} to $host! -> $response")
        }
    }
}