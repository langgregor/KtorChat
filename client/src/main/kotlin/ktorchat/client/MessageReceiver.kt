package ktorchat.client

import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.jetty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.channels.Channel
import ktorchat.common.Configuration
import ktorchat.common.MessageData
import ktorchat.common.ServerStartFinishedCallback

/**
 * Receives messages from server and puts them into a channel.
 */
class MessageReceiver(private val sourceHost: String) {
    val channel = Channel<MessageData>()
    private var server: JettyApplicationEngine? = null

    fun start() {
        server = embeddedServer(
            Jetty,
            port = Configuration.CLIENT_PORT,
            module = { module() })
        server?.start(wait = true)
    }

    fun stop() {
        server?.stop()
        channel.close()
    }

    private fun Application.module() {
        installPlugins()
        configureRoutes()
    }

    private fun Application.installPlugins() {
        install(ContentNegotiation) {
            gson()
        }
        install(ServerStartFinishedCallback) {
            callback {
                println("Receiver is running.")
            }
        }
    }

    private fun Application.configureRoutes() {
        routing {
            post("/receive") {
                val message = call.receive<MessageData>()
                if (call.request.local.remoteHost == sourceHost) {
                    channel.send(message)
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }
        }
    }
}
