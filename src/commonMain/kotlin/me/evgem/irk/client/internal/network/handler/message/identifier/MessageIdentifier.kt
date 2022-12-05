package me.evgem.irk.client.internal.network.handler.message.identifier

import me.evgem.irk.client.internal.model.message.AbstractMessage
import me.evgem.irk.client.internal.model.message.EmptyMessage
import me.evgem.irk.client.internal.model.message.ReplyMessage
import me.evgem.irk.client.internal.model.message.UnknownMessage
import me.evgem.irk.client.internal.model.message.misc.MessageCommand
import me.evgem.irk.client.internal.model.message.misc.NumericReply

internal interface MessageIdentifier {
    fun identify(message: UnknownMessage): AbstractMessage
}

internal fun MessageIdentifier(messageFactories: List<MessageFactory<*>>): MessageIdentifier =
    DefaultMessageIdentifier(messageFactories)

private class DefaultMessageIdentifier(
    messageFactories: List<MessageFactory<*>>,
) : MessageIdentifier {

    private val commandsMap: Map<String, MessageCommand> = MessageCommand.values().associateBy { it.rfcName }
    private val numericRepliesMap: Map<Int, NumericReply> = NumericReply.values().associateBy { it.code }
    private val messageFactoriesMap: Map<MessageCommand, MessageFactory<*>> =
        messageFactories.associateBy { it.messageCommand }

    override fun identify(message: UnknownMessage): AbstractMessage {
        if (message.command.isEmpty()) {
            return EmptyMessage
        }
        message.command.toIntOrNull()?.let(numericRepliesMap::get)?.let { numericReply ->
            return ReplyMessage(
                numericReply = numericReply,
                stringReply = message.trailingParam?.toString(),
            )
        }
        commandsMap[message.command]?.let(messageFactoriesMap::get)?.let {
            return it.create(message)
        }
        return message
    }
}
