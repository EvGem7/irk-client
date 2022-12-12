package me.evgem.irk.client.internal.network.handler.message.identifier.factory

import me.evgem.irk.client.internal.network.handler.message.identifier.MessageFactory
import me.evgem.irk.client.model.message.JoinMessage
import me.evgem.irk.client.model.message.UnknownMessage
import me.evgem.irk.client.model.message.misc.MessageCommand
import me.evgem.irk.client.util.ByteArrayWrapper

internal class JoinMessageFactory : MessageFactory<JoinMessage> {

    override val messageCommand: MessageCommand
        get() = MessageCommand.JOIN

    override fun create(unknownMessage: UnknownMessage): JoinMessage {
        val lists = unknownMessage
            .allParams
            .map(ByteArrayWrapper::toString)
            .map { it.split(',') }
        return JoinMessage(
            channels = lists.getOrElse(0) { emptyList() },
            keys = lists.getOrElse(1) { emptyList() },
            prefix = unknownMessage.prefix,
        )
    }
}