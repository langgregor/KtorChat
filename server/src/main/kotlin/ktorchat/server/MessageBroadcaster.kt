package ktorchat.server

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import ktorchat.common.Configuration
import ktorchat.common.MessageData

class MessageBroadcaster {
    private var client: HttpClient = HttpClient() {
        defaultRequest {
            url {
                protocol = URLProtocol.HTTP
                port = Configuration.CLIENT_PORT
                path("/receive")
            }
        }
        install(ContentNegotiation) {
            json()
        }
    }

    suspend fun broadcast(message: MessageData, hosts: List<String>) {
        for (host in hosts) {
            client.post {
                url {
                    this.host = host
                }
                setBody(message)
            }
        }
    }
}