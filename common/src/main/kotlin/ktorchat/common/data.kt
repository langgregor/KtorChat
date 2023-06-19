package ktorchat.common

import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class MessageData(val user : UserData, val message : String, @Contextual val date : Date)

@Serializable
data class UserData(val id: String)

@Serializable
data class UserDetails(val host: String, @Contextual val loginDate : Date)