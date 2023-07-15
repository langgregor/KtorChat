package ktorchat.server

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import ktorchat.common.Configuration
import ktorchat.common.MessageData
import java.util.*

class MessageDistributor {
    private var client: HttpClient = HttpClient {
        defaultRequest {
            contentType(ContentType.Application.Json)
        }
        install(ContentNegotiation) {
            gson {
                serializeNulls()
            }
        }
        install(HttpTimeout) {
            requestTimeoutMillis = 2000
            connectTimeoutMillis = 2000
            socketTimeoutMillis = 2000
        }
    }

    suspend fun distribute(
        message: MessageData,
        uuidToHostResolver: (UUID) -> String?,
        fallbackTargetProducer: () -> Set<UUID> = { emptySet() }
    ) {
        val targets = message.targetUsers.ifEmpty { fallbackTargetProducer() }

        var count = 0
        targets.mapNotNull(uuidToHostResolver).forEach { clientReceiverHost ->
            val response = client.post("receive") {
                host = clientReceiverHost
                port = Configuration.CLIENT_PORT
                setBody(message)
            }
            count++
            println("Distributed message from ${uuidToHostResolver(message.sourceUser)} to $clientReceiverHost! -> $response")
        }
        println("Distributed $count messages!")
    }
}