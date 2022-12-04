package me.evgem.irk.client

import io.ktor.utils.io.core.use
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.withTimeoutOrNull
import me.evgem.irk.client.internal.di.IrkClientComponent
import me.evgem.irk.client.internal.model.message.NickMessage
import me.evgem.irk.client.internal.model.message.PasswordMessage
import me.evgem.irk.client.internal.model.message.UserMessage
import me.evgem.irk.client.internal.util.Log

class IrkClient(
    workCoroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {

    private val component = IrkClientComponent(workCoroutineDispatcher)

    suspend fun test(password: String) {
        Log("start up")
        component.messageHandlerFactory().createMessageHandler(
            hostname = "irc.ppy.sh",
            port = 6667,
        ).use {
            it.sendMessage(PasswordMessage(password))
            it.sendMessage(NickMessage("EvGem"))
            it.sendMessage(
                UserMessage(
                    user = "EvGem",
                    mode = 0,
                    realName = "EvGem",
                )
            )
            withTimeoutOrNull(15000L) {
                it.receiveMessages().collect()
            }
        }
    }
}