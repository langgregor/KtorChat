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

class MessageServer {
    private val messageChannel = Channel<MessageData>()
    private val messageDistributor = MessageDistributor()
    private val userManager = UserManager()

    fun start() = embeddedServer(
        Jetty,
        port = Configuration.SERVER_PORT,
        module = { module() }).start(wait = true)

    private suspend fun distributeMessages() {
        println("Start DistributeMessage Loop on Thread ${Thread.currentThread().name}")
        for (msg in messageChannel) {
            val replacementMessage =
                if (msg.targetUsers.isEmpty()) {
                    MessageData(userManager.getAllUsers().minus(msg.sourceUser), msg.sourceUser, msg.message)
                } else {
                    msg
                }

            messageDistributor.distribute(replacementMessage) {
                userManager.getUser(it)?.host
            }
        }
    }

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
                println("Server running and listening on ${it.config.host}:${it.config.port}.")
                distributeMessages()
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


