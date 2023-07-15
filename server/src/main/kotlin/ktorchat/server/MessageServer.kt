package ktorchat.server

import io.ktor.http.*
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.jetty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.ratelimit.*
import io.ktor.server.plugins.requestvalidation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.channels.Channel
import ktorchat.common.*
import java.util.*
import kotlin.time.Duration.Companion.seconds

class MessageServer(private val userManager: UserManager) {
    companion object {
        private const val RATE_LIMIT_LOGIN = "login"
        private const val RATE_LIMIT_MESSAGE = "message"
    }

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
        install(RateLimit) {
            register(RateLimitName(RATE_LIMIT_LOGIN)) {
                rateLimiter(limit = 5, refillPeriod = 10.seconds)
            }
            register(RateLimitName(RATE_LIMIT_MESSAGE)) {
                rateLimiter(limit = 20, refillPeriod = 10.seconds)
            }
        }
        install(RequestValidation) {
            validateUserData()
            validateMessageData()
        }
        install(StatusPages) {
            exception<RequestValidationException> { call, cause ->
                call.respond(HttpStatusCode.BadRequest, cause.reasons.joinToString())
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
            rateLimit(RateLimitName(Companion.RATE_LIMIT_LOGIN)) {
                post("/login") {
                    val userdata = call.receive<UserData>()
                    val response = userManager.login(userdata, call.request.local.remoteHost)
                    call.respond(if (response.success) HttpStatusCode.OK else HttpStatusCode.Unauthorized, response)
                }
            }

            post("/logout") {
                val data = call.receive<LogoutData>()
                call.respondIfAuthorized(data.uuid) {
                    userManager.logout(data.uuid)
                }
            }

            rateLimit(RateLimitName(RATE_LIMIT_MESSAGE)) {
                post("/message") {
                    val message = call.receive<MessageData>()
                    call.respondIfAuthorized(message.sourceUser) {
                        message.userName = userManager.getUser(message.sourceUser)?.username
                        messageChannel.send(message)
                    }
                }
            }
        }
    }

    private suspend fun ApplicationCall.respondIfAuthorized(userUuid: UUID, block: suspend () -> Unit) {
        if (userManager.isAuthorized(userUuid, this.request.local.remoteHost)) {
            block()
            this.respond(HttpStatusCode.OK)
        } else {
            this.respond(HttpStatusCode.Unauthorized)
        }
    }
}


