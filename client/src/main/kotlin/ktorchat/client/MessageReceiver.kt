package ktorchat.client

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import ktorchat.common.MessageData
import kotlinx.coroutines.channels.Channel
import ktorchat.common.Configuration

/**
 * Receives messages from server and puts them into a channel.
 */
class MessageReceiver() {
    val channel = Channel<MessageData>()

    init {
        embeddedServer(CIO, host = Configuration.CLIENT_HOST, port = Configuration.CLIENT_PORT,  module = { module() }).start(wait = true)
    }

    private fun Application.module() {
        installPlugins()
        configureRoutes()
    }

    private fun Application.installPlugins() {
        install(ContentNegotiation) {
            json()
        }
    }

    private fun Application.configureRoutes() {
        routing {
            post("/receive") {
                val message = call.receive<MessageData>()
                channel.send(message)
            }
        }
    }
}
