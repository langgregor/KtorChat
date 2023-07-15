package ktorchat.client

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import ktorchat.common.MessageData
import ktorchat.common.UserData
import java.text.SimpleDateFormat
import java.util.*

fun main() {
    val userName = "Gregor" // TODO: input from cmd
    val serverHost = "192.168.1.140" // TODO: input from cmd

    val sender = MessageSender(serverHost)
    val receiver = MessageReceiver()

    runBlocking {
        val userId = sender.login(UserData(userName)) ?: return@runBlocking

        launch(Dispatchers.Default) {
            println("Start Receiver on Thread ${Thread.currentThread().name}")
            receiver.start()
        }

        launch(Dispatchers.Default) {
            println("Start ChannelLoop on Thread ${Thread.currentThread().name}")
            val dateFormat = SimpleDateFormat("hh:mm:ss")
            for (m in receiver.channel) {
                println("(${dateFormat.format(Date())}) [${m.sourceUser}] \"${m.message}\"")
            }
        }

        launch(Dispatchers.IO) {
            println("Start MessageLoop on thread ${Thread.currentThread().name}")
            var msg = ""
            while (msg != "exit") {
                delay(1000)
                msg = readln()
                sender.send(MessageData(emptySet(), userId, msg))
            }
        }
    }
}