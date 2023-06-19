package ktorchat.server

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import ktorchat.common.Configuration
import ktorchat.common.MessageData
import ktorchat.common.UserData
import ktorchat.common.UserDetails
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MessageServer() {
    private val messageChannel = Channel<MessageData>()
    private val messageBroadcaster = MessageBroadcaster()
    private val users = ConcurrentHashMap<UserData, UserDetails>()

    init {
        configureServer()
    }

    fun startBroadcasting() {
        CoroutineScope(Dispatchers.Default).launch {
            for (msg in messageChannel) {
                val hosts = users.map { (_, detail) -> detail.host }
                messageBroadcaster.broadcast(msg, hosts)
            }
        }
    }

    private fun configureServer() = embeddedServer(
        CIO,
        host = Configuration.SERVER_HOST,
        port = Configuration.SERVER_PORT,
        module = { module() }).start(wait = true)

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
            post("/login") {
                val user = call.receive<UserData>()
                if (!users.contains(user)) {
                    call.respond(HttpStatusCode.Unauthorized)
                }
                val details = UserDetails(call.request.local.localHost, Date())
                users[user] = details
            }

            post("/message") {
                val message = call.receive<MessageData>()
                messageChannel.send(message)
            }
        }
    }
}

