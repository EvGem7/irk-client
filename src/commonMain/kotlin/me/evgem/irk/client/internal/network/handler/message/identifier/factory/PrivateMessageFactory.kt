package me.evgem.irk.client.internal.network.handler.message.identifier.factory

import me.evgem.irk.client.internal.network.handler.message.identifier.MessageFactory
import me.evgem.irk.client.model.message.PrivateMessage
import me.evgem.irk.client.model.message.UnknownMessage
import me.evgem.irk.client.model.message.misc.MessageCommand

internal class PrivateMessageFactory : MessageFactory<PrivateMessage> {

    override val messageCommand: MessageCommand
        get() = MessageCommand.PRIVMSG

    override fun create(unknownMessage: UnknownMessage): PrivateMessage {
        return PrivateMessage(
            prefix = unknownMessage.prefix,
            target = unknownMessage.allStringParams.firstOrNull().orEmpty(),
            text = unknownMessage.trailingParam?.toString().orEmpty(),
        )
    }
}