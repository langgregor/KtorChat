package ktorchat.server

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
import ktorchat.common.UserData

class MessageServer(private val userManager: UserManager) {
    val messageChannel = Channel<MessageData>()

    fun start() =
        embeddedServer(
            Jetty,
            port = Configuration.SERVER_PORT,
            module = { module() }).start(wait = true)

    private fun Application.module() {
        installPlugins()
        configureRoutes()
    }

    private fun Application.installPlugins() {
        install(ContentNegotiation) {
            gson {
                serializeNulls()
            }
        }
        install(ServerStartFinishedCallback) {
            callback {
                println("Server is running.")
            }
        }
    }

    private fun Application.configureRoutes() {
        routing {
            post("/login") {
                val userdata = call.receive<UserData>()
                val response = userManager.login(userdata, call.request.local.remoteHost)
                call.respond(if (response.success) HttpStatusCode.OK else HttpStatusCode.Unauthorized, response)
            }

            post("/message") {
                val message = call.receive<MessageData>()
                if (userManager.isAuthorized(message.sourceUser, call.request.local.remoteHost)) {
                    messageChannel.send(message)
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }
        }
    }
}


