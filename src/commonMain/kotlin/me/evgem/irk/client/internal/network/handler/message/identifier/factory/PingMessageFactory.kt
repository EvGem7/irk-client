package me.evgem.irk.client.internal.network.handler.message.identifier.factory

import me.evgem.irk.client.internal.network.handler.message.identifier.MessageFactory
import me.evgem.irk.client.model.message.PingMessage
import me.evgem.irk.client.model.message.UnknownMessage
import me.evgem.irk.client.model.message.misc.MessageCommand

internal class PingMessageFactory : MessageFactory<PingMessage> {

    override val messageCommand: MessageCommand
        get() = MessageCommand.PING

    override fun create(unknownMessage: UnknownMessage): PingMessage {
        return PingMessage(
            server1 = unknownMessage.allStringParams.getOrNull(0).orEmpty(),
            server2 = unknownMessage.allStringParams.getOrNull(1).orEmpty(),
            prefix = unknownMessage.prefix,
        )
    }
}