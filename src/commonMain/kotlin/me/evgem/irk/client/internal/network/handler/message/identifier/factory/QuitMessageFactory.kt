package me.evgem.irk.client.internal.network.handler.message.identifier.factory

import me.evgem.irk.client.internal.model.message.QuitMessage
import me.evgem.irk.client.internal.model.message.UnknownMessage
import me.evgem.irk.client.internal.model.message.misc.MessageCommand
import me.evgem.irk.client.internal.network.handler.message.identifier.MessageFactory

internal class QuitMessageFactory : MessageFactory<QuitMessage> {

    override val messageCommand: MessageCommand
        get() = MessageCommand.QUIT

    override fun create(unknownMessage: UnknownMessage): QuitMessage {
        return QuitMessage(unknownMessage.trailingParam?.array?.decodeToString().orEmpty())
    }
}