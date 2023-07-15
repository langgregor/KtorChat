package ktorchat.server

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    val userManager = UserManager()
    val messageServer = MessageServer(userManager)
    val messageDistributor = MessageDistributor()

    runBlocking {
        launch(Dispatchers.Default) {
            println("Start MessageServer on Thread ${Thread.currentThread().name}")
            messageServer.start()
        }

        launch(Dispatchers.Default) {
            println("Start DistributeMessage Loop on Thread ${Thread.currentThread().name}")
            for (msg in messageServer.messageChannel) {
                messageDistributor.distribute(msg,
                    uuidToHostResolver = {
                        userManager.getUser(it)?.host
                    },
                    fallbackTargetProducer = {
                        userManager.getAllUsers().minus(msg.sourceUser)
                    })
            }
        }
    }
}