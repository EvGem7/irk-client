package me.evgem.irk.client

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.evgem.irk.client.internal.Scoped
import me.evgem.irk.client.internal.network.handler.message.MessageHandler
import me.evgem.irk.client.model.ChannelNameWithKey
import me.evgem.irk.client.model.User
import me.evgem.irk.client.model.message.AbstractMessage
import me.evgem.irk.client.model.message.JoinMessage
import me.evgem.irk.client.model.message.PartAllMessage
import me.evgem.irk.client.model.message.PartMessage
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
    val me: User,
) : Scoped by messageHandler {

    var motd: String = motd
        private set

    private val channels = mutableMapOf<String, IrkChannel>()
    private val channelsMutex = Mutex()

    val messages: Flow<AbstractMessage> get() = messageHandler.receiveMessages()

    suspend fun quit(quitMessage: String = "") {
        messageHandler.sendMessage(QuitMessage(quitMessage))
    }

    suspend fun joinChannels(channels: Sequence<ChannelNameWithKey>): List<IrkChannel> = channelsMutex.withLock {
        val names = channels.map { it.channelName }.toList()
        val keys = channels.mapNotNull { it.key }.toList()
        messageHandler.sendMessage(JoinMessage(names, keys))
        return messageHandler
            .receiveMessages()
            .onEach { it.throwIfError() }
            .transformToChannel()
            .take(names.size)
            .onEach { this.channels[it.name] = it }
            .toList()
    }

    suspend fun partAll(): Unit = channelsMutex.withLock {
        messageHandler.sendMessage(PartAllMessage)
        messageHandler
            .receiveMessages()
            .onEach { it.throwIfError() }
            .filterIsInstance<PartMessage>()
            .filter { it.user == me }
            .takeWhile { message ->
                message.channels.forEach {
                    this.channels.remove(it)
                }
                this.channels.isNotEmpty()
            }
            .collect()
    }

    private fun Flow<AbstractMessage>.transformToChannel(): Flow<IrkChannel> {
        var name = ""
        var topic = ""
        val users = mutableSetOf<User>()
        return transform { message ->
            if (message is JoinMessage && message.user == me) {
                name = message.channels.firstOrNull().orEmpty()
            }
            if (message is ReplyMessage && message.numericReply == KnownNumericReply.RPL_TOPIC) {
                topic = message.replyStringParams.getOrNull(1).orEmpty()
            }
            if (message is ReplyMessage && message.numericReply == KnownNumericReply.RPL_NAMREPLY) {
                users += message
                    .replyStringParams
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IrkServer) return false

        if (hostname != other.hostname) return false
        if (port != other.port) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hostname.hashCode()
        result = 31 * result + port
        return result
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
