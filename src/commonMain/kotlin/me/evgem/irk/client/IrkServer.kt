package me.evgem.irk.client

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import me.evgem.irk.client.internal.model.message.QuitMessage
import me.evgem.irk.client.internal.network.handler.message.MessageHandler

class IrkServer internal constructor(
    private val messageHandler: MessageHandler,
    val welcomeMessage: String,
    motd: String,
) {

    var motd: String = motd
        private set

    // TODO
    val messages: Flow<Unit> get() = messageHandler.receiveMessages().map {}

    suspend fun quit(quitMessage: String = "") {
        messageHandler.sendMessage(QuitMessage(quitMessage))
    }
}