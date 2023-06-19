package ktorchat.client

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ktorchat.common.Configuration
import ktorchat.common.MessageData
import ktorchat.common.UserData
import java.text.SimpleDateFormat
import java.util.*

fun main() {
    val userName = "Greor"

    val sender = MessageSender()
    val receiver = MessageReceiver()

    // sender.login(UserData(userName))

    runBlocking {
        launch {
            delay(1000)
            val msg = readln()
            sender.send(MessageData(UserData(userName), msg, Date()))
        }

        launch {
            val dateFormat = SimpleDateFormat("hh:mm:ss")
            for (m in receiver.channel) {
                println("(${dateFormat.format(m.date)}) [${m.user.id}] \"${m.message}\"")
            }
        }
    }
}