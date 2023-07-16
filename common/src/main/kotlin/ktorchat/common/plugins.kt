package ktorchat.common

import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking

class ServerReadyCallbackConfiguration() {
    var block: suspend CoroutineScope.(ApplicationEnvironment) -> Unit = {}

    fun callback(block: suspend CoroutineScope.(ApplicationEnvironment) -> Unit) {
        this.block = block
    }
}

val ServerReadyCallback = createApplicationPlugin(
    name = "ServerStatusLogging", createConfiguration = ::ServerReadyCallbackConfiguration
) {
    on(MonitoringEvent(ServerReady)) {
        runBlocking {
            pluginConfig.block(this, it)
        }
    }
}