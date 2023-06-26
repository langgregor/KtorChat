package ktorchat.common

import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

class ServerStartFinishedCallbackConfiguration() {
    var block: suspend CoroutineScope.(ApplicationEnvironment) -> Unit = {}

    fun callback(block: suspend CoroutineScope.(ApplicationEnvironment) -> Unit) {
        this.block = block
    }
}

val ServerStartFinishedCallback = createApplicationPlugin(
    name = "ServerStatusLogging", createConfiguration = ::ServerStartFinishedCallbackConfiguration
) {
    on(MonitoringEvent(ServerReady)) {
        runBlocking {
            pluginConfig.block(this, it)
        }
    }
}