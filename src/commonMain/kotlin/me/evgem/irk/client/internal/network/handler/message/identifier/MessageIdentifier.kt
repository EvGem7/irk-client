package me.evgem.irk.client.internal.network.handler.message.identifier

import me.evgem.irk.client.internal.util.orElse
import me.evgem.irk.client.model.message.AbstractMessage
import me.evgem.irk.client.model.message.EmptyMessage
import me.evgem.irk.client.model.message.ReplyMessage
import me.evgem.irk.client.model.message.UnknownMessage
import me.evgem.irk.client.model.message.misc.KnownNumericReply
import me.evgem.irk.client.model.message.misc.MessageCommand
import me.evgem.irk.client.model.message.misc.UnknownNumericReply

internal interface MessageIdentifier {
    fun identify(message: UnknownMessage): AbstractMessage
}

internal fun MessageIdentifier(messageFactories: List<MessageFactory<*>>): MessageIdentifier =
    DefaultMessageIdentifier(messageFactories)

private class DefaultMessageIdentifier(
    messageFactories: List<MessageFactory<*>>,
) : MessageIdentifier {

    private val commandsMap: Map<String, MessageCommand> = MessageCommand.values().associateBy { it.rfcName }
    private val numericRepliesMap: Map<Int, KnownNumericReply> = KnownNumericReply.values().associateBy { it.code }
    private val messageFactoriesMap: Map<MessageCommand, MessageFactory<*>> =
        messageFactories.associateBy { it.messageCommand }

    override fun identify(message: UnknownMessage): AbstractMessage {
        if (message.command.isEmpty()) {
            return EmptyMessage
        }
        message.command.toIntOrNull()?.let { code ->
            val numericReply = numericRepliesMap[code].orElse { UnknownNumericReply(code) }
            return ReplyMessage(
                numericReply = numericReply,
                prefix = message.prefix,
                middleParams = message.middleParams,
                trailingParam = message.trailingParam,
            )
        }
        commandsMap[message.command]?.let(messageFactoriesMap::get)?.let {
            return it.create(message)
        }
        return message
    }
}
