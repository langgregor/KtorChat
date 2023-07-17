package ktorchat.server

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.*
import io.ktor.http.*
import io.ktor.serialization.gson.*
import ktorchat.common.Configuration
import ktorchat.common.MessageData
import java.util.*

class MessageDistributor(
    val onError: (MessageData, UUID) -> Unit = { _, _ -> },
    val onTimout: (MessageData, UUID) -> Unit = { _, _ -> },
    val fallbackTargetProducer: (MessageData) -> Set<UUID> = { emptySet() }
) {

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
        }
        install(HttpRequestRetry) {
            maxRetries = 2
            retryOnExceptionIf { _, cause ->
                cause.unwrapCancellationException() is HttpRequestTimeoutException
            }
        }
    }

    suspend fun distribute(message: MessageData, uuidToHostResolver: (UUID) -> String?) {
        val targets = message.targetUsers.ifEmpty { fallbackTargetProducer(message) }

        targets.forEach { uuid ->
            val clientReceiverHost = uuidToHostResolver(uuid) ?: return@forEach
            val response: HttpResponse = try {
                client.post("receive") {
                    host = clientReceiverHost
                    port = Configuration.CLIENT_PORT
                    setBody(message)
                }
            } catch (e: HttpRequestTimeoutException) {
                println("Timout to $clientReceiverHost")
                onTimout(message, uuid)
                return@forEach
            }

            if (response.status.value.let { it in 500..599 }) {
                println("Error to $clientReceiverHost")
                onError(message, uuid)
            } else {
                println("Distributed message from ${uuidToHostResolver(message.sourceUser)} to $clientReceiverHost! -> $response")
            }
        }
    }
}