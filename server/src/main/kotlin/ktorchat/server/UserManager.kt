package ktorchat.server

import io.ktor.util.collections.*
import ktorchat.common.LoginResponse
import ktorchat.common.UserData
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class UserManager {
    private val users = ConcurrentHashMap<UUID, User>()
    private val usernames = ConcurrentSet<String>()
    private val hosts = ConcurrentSet<String>()

    fun login(userdata: UserData, host: String): LoginResponse {
        val username = userdata.username

        if (usernames.contains(username)) {
            val loginResponse = LoginResponse(false, null, "Duplicate username")
            println("Login failed: User:$username, Host:$host, Response:$loginResponse")
            return loginResponse
        }

        if (hosts.contains(host)) {
            val loginResponse = LoginResponse(false, null, "Duplicate host")
            println("Login failed: User:$username, Host:$host, Response:$loginResponse")
            return loginResponse
        }

        hosts.add(host)
        usernames.add(username)

        val uuid = UUID.nameUUIDFromBytes(username.encodeToByteArray())
        users[uuid] = User(username, host)

        println("Login success: User:$username, Host:$host, UUID:$uuid")
        return LoginResponse(true, uuid, null)
    }

    fun isAuthorized(userId: UUID, host: String): Boolean {
        val user = users[userId] ?: return false
        return user.loggedIn && user.host == host
    }

    fun getAllUsers(): Set<UUID> = users.keys

    fun getUser(userId: UUID): User? = users[userId]
}

data class User(val username: String, val host: String, val lastLogin: Date = Date(), val loggedIn: Boolean = true)