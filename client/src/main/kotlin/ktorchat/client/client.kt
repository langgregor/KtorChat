package ktorchat.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ktorchat.common.LogoutData
import ktorchat.common.MessageData
import ktorchat.common.UserData
import java.text.SimpleDateFormat
import java.util.*

fun main() {
    val userName = readUsername()
    val serverHost = readHostAddress()

    val sender = MessageSender(serverHost)
    val receiver = MessageReceiver(serverHost)

    runBlocking {
        val uuid = sender.login(UserData(userName)) ?: return@runBlocking

        launch(Dispatchers.Default) {
            println("Start Receiver on Thread ${Thread.currentThread().name}")
            receiver.start()
        }

        launch(Dispatchers.Default) {
            println("Start ChannelLoop on Thread ${Thread.currentThread().name}")
            val dateFormat = SimpleDateFormat("hh:mm:ss")
            for (m in receiver.channel) {
                println("(${dateFormat.format(Date())}) [${m.userName ?: m.sourceUser}] \"${m.message}\"")
            }
        }

        launch(Dispatchers.IO) {
            println("Start MessageLoop on thread ${Thread.currentThread().name}")
            while (true) {
                delay(1000)
                val msg = readln()
                sender.send(MessageData(emptySet(), uuid, msg))
                if (msg == "exit") break
            }
            receiver.stop()
            sender.logout(LogoutData(uuid))
        }
    }
}

fun readHostAddress(): String {
    print("Enter Hostname: ")
    return readln()
}

fun readUsername(): String {
    print("Enter Username: ")
    return readln()
}