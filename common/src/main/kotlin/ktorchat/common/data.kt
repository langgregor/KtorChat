package ktorchat.common

import java.util.*

data class MessageData(
    val targetUsers: Set<UUID>,
    val sourceUser: UUID,
    val message: String,
    var userName: String? = null
)

data class UserData(val username: String)

data class LogoutData(val uuid: UUID)

data class LoginResponse(val success: Boolean, val id: UUID?, val errorMessage: String?)