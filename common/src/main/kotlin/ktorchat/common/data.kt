package ktorchat.common

import java.util.*

data class MessageData(val targetUsers: Set<UUID>, val sourceUser: UUID, val message: String)

data class UserData(val username: String)

data class LoginResponse(val success: Boolean, val id: UUID?, val errorMessage: String?)