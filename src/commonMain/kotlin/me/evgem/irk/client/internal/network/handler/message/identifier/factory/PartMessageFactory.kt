package me.evgem.irk.client.internal.network.handler.message.identifier.factory

import me.evgem.irk.client.internal.network.handler.message.identifier.MessageFactory
import me.evgem.irk.client.model.message.PartMessage
import me.evgem.irk.client.model.message.UnknownMessage
import me.evgem.irk.client.model.message.misc.MessageCommand

internal class PartMessageFactory : MessageFactory<PartMessage> {

    override val messageCommand: MessageCommand
        get() = MessageCommand.PART

    override fun create(unknownMessage: UnknownMessage): PartMessage {
        return PartMessage(
            channels = unknownMessage.allStringParams.firstOrNull().orEmpty().split(','),
            partMessage = unknownMessage.allStringParams.getOrNull(1),
            prefix = unknownMessage.prefix,
        )
    }
}