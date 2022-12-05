package me.evgem.irk.client.internal.network.handler.message.identifier

import me.evgem.irk.client.internal.model.message.AbstractMessage
import me.evgem.irk.client.internal.model.message.UnknownMessage
import me.evgem.irk.client.internal.model.message.misc.MessageCommand

internal interface MessageFactory<T : AbstractMessage> {

    val messageCommand: MessageCommand

    fun create(unknownMessage: UnknownMessage): T
}