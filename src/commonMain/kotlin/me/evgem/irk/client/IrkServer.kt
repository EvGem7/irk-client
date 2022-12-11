package me.evgem.irk.client

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import me.evgem.irk.client.internal.network.handler.message.MessageHandler
import me.evgem.irk.client.model.ChannelNameWithKey
import me.evgem.irk.client.model.message.AbstractMessage
import me.evgem.irk.client.model.message.JoinMessage
import me.evgem.irk.client.model.message.PartAllMessage
import me.evgem.irk.client.model.message.QuitMessage
import me.evgem.irk.client.model.message.ReplyMessage
import me.evgem.irk.client.model.message.misc.NumericReply
import me.evgem.irk.client.model.message.throwIfError

class IrkServer internal constructor(
    internal val messageHandler: MessageHandler,
    val welcomeMessage: String,
    motd: String,
) {

    var motd: String = motd
        private set

    val messages: Flow<AbstractMessage> get() = messageHandler.receiveMessages()

    suspend fun quit(quitMessage: String = "") {
        messageHandler.sendMessage(QuitMessage(quitMessage))
    }

    suspend fun joinChannels(channels: Sequence<ChannelNameWithKey>): List<IrkChannel> {
        val names = channels.map { it.channelName }.toList()
        val keys = channels.mapNotNull { it.key }.toList()
        messageHandler.sendMessage(JoinMessage(names, keys))
        return messageHandler
            .receiveMessages()
            .filterIsInstance<ReplyMessage>()
            .onEach { it.throwIfError() }
            .filter { it.numericReply == NumericReply.RPL_TOPIC }
            .take(names.size)
            .map { topicMessage -> createChannel(topicMessage) }
            .toList()
    }

    private fun createChannel(topicMessage: ReplyMessage): IrkChannel {
        return IrkChannel(
            server = this,
            topic = topicMessage.trailingParam?.toString().orEmpty(),
            name = topicMessage.middleParams.firstOrNull()?.toString().orEmpty(),
        )
    }

//    not working at irc.ppy.sh
//    suspend fun partAll() {
//        messageHandler.sendMessage(PartAllMessage)
//        messageHandler.receiveMessages()
//    }
}

suspend fun IrkServer.joinChannel(channelName: String, key: String? = null): IrkChannel {
    return joinChannel(ChannelNameWithKey(channelName, key))
}

suspend fun IrkServer.joinChannel(channel: ChannelNameWithKey): IrkChannel {
    return joinChannels(channel).first()
}

suspend fun IrkServer.joinChannels(vararg channels: String): List<IrkChannel> {
    return joinChannelsByName(channels.asSequence())
}

suspend fun IrkServer.joinChannels(vararg channels: ChannelNameWithKey): List<IrkChannel> {
    return joinChannels(channels.asSequence())
}

suspend fun IrkServer.joinChannelsByName(channels: Sequence<String>): List<IrkChannel> {
    val seq = channels.map { ChannelNameWithKey(it, null) }
    return joinChannels(seq)
}

suspend fun IrkServer.joinChannelsByName(channels: Iterable<String>): List<IrkChannel> {
    return joinChannelsByName(channels.asSequence())
}

suspend fun IrkServer.joinChannels(channels: Iterable<ChannelNameWithKey>): List<IrkChannel> {
    return joinChannels(channels.asSequence())
}
