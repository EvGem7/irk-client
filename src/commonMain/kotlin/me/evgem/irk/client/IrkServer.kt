package me.evgem.irk.client

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transform
import me.evgem.irk.client.internal.Scoped
import me.evgem.irk.client.internal.network.handler.message.MessageHandler
import me.evgem.irk.client.model.ChannelNameWithKey
import me.evgem.irk.client.model.User
import me.evgem.irk.client.model.message.AbstractMessage
import me.evgem.irk.client.model.message.JoinMessage
import me.evgem.irk.client.model.message.QuitMessage
import me.evgem.irk.client.model.message.ReplyMessage
import me.evgem.irk.client.model.message.misc.KnownNumericReply
import me.evgem.irk.client.model.message.throwIfError

class IrkServer internal constructor(
    internal val messageHandler: MessageHandler,
    val hostname: String,
    val port: Int,
    val welcomeMessage: String,
    motd: String,
) : Scoped by messageHandler {

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
            .onEach { it.throwIfError() }
            .transformToChannel()
            .take(names.size)
            .toList()
    }

//    not working at irc.ppy.sh, can't debug
//    suspend fun partAll() {
//        messageHandler.sendMessage(PartAllMessage)
//        messageHandler.receiveMessages()
//    }

    private fun Flow<AbstractMessage>.transformToChannel(): Flow<IrkChannel> {
        var name = ""
        var topic = ""
        val users = mutableSetOf<User>()
        return transform { message ->
            if (message is ReplyMessage && message.numericReply == KnownNumericReply.RPL_TOPIC) {
                name = message.allStringParams.run { getOrNull(lastIndex - 1) }.orEmpty()
                topic = message.allStringParams.run { getOrNull(lastIndex - 0) }.orEmpty()
            }
            if (message is ReplyMessage && message.numericReply == KnownNumericReply.RPL_NAMREPLY) {
                users += message
                    .allStringParams
                    .lastOrNull()
                    .orEmpty()
                    .splitToSequence(' ')
                    .filter { it.isNotBlank() }
                    .map { User.fromNameReply(it) }
            }
            if (message is ReplyMessage && message.numericReply == KnownNumericReply.RPL_ENDOFNAMES) {
                IrkChannel(
                    server = this@IrkServer,
                    name = name,
                    topic = topic,
                    users = users,
                ).let { emit(it) }
                name = ""
                topic = ""
                users.clear()
            }
        }
    }

    override fun toString(): String {
        return "IrkServer(hostname='$hostname', port=$port, welcomeMessage='$welcomeMessage', motd='$motd')"
    }
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
