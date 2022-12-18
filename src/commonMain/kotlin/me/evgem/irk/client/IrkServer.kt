package me.evgem.irk.client

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.evgem.irk.client.internal.Scoped
import me.evgem.irk.client.internal.network.handler.message.MessageHandler
import me.evgem.irk.client.model.ChannelName
import me.evgem.irk.client.model.ChannelNameWithKey
import me.evgem.irk.client.model.User
import me.evgem.irk.client.model.message.AbstractMessage
import me.evgem.irk.client.model.message.JoinMessage
import me.evgem.irk.client.model.message.PartAllMessage
import me.evgem.irk.client.model.message.PartMessage
import me.evgem.irk.client.model.message.PingMessage
import me.evgem.irk.client.model.message.PongMessage
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

    private val channels = mutableMapOf<ChannelName, IrkChannel>()
    private val channelsMutex = Mutex()

    init {
        collectPartMessages()
        observePing()
    }

    private fun observePing() {
        messages.filterIsInstance<PingMessage>().onEach {
            messageHandler.sendMessage(PongMessage(it.server1))
        }.launchIn(coroutineScope)
    }

    private fun collectPartMessages() {
        coroutineScope.launch {
            receivePartMessages(withLock = true, throwOnError = false).collect()
        }
    }

    suspend fun getChannels(): Set<IrkChannel> = channelsMutex.withLock { channels.values.toSet() }

    val messages: Flow<AbstractMessage> get() = messageHandler.receiveMessages()

    suspend fun quit(quitMessage: String = "") {
        messageHandler.sendMessage(QuitMessage(quitMessage))
    }

    suspend fun joinChannels(channels: Sequence<ChannelNameWithKey>): List<IrkChannel> = channelsMutex.withLock {
        val withoutJoined = channels.filter { !this.channels.containsKey(it.channelName) }
        val names = withoutJoined.map { it.channelName.toString() }.toList()
        val keys = withoutJoined.mapNotNull { it.key }.toList()
        if (names.isNotEmpty()) {
            messageHandler.sendMessage(JoinMessage(names, keys))
            messageHandler
                .receiveMessages()
                .onEach { it.throwIfError() }
                .transformToChannel()
                .take(names.size)
                .collect { this.channels[it.name] = it }
        }
        channels.mapNotNull { this.channels[it.channelName] }.toList()
    }

    suspend fun partAll(): Unit = channelsMutex.withLock {
        messageHandler.sendMessage(PartAllMessage)
        receivePartMessages(withLock = false, throwOnError = true)
            .takeWhile { this.channels.isNotEmpty() }
            .collect()
    }

    suspend fun partWithMessage(message: String?, channelNames: Set<ChannelName>): Unit = channelsMutex.withLock {
        if (channelNames.isEmpty()) {
            return@withLock
        }
        PartMessage(
            channels = channelNames.asSequence().map { it.toString() }.toList(),
            partMessage = message,
        ).let { messageHandler.sendMessage(it) }
        val set = channelNames.toMutableSet()
        receivePartMessages(withLock = false, throwOnError = true).takeWhile { message ->
            message.channels.asSequence().map(::ChannelName).forEach(set::remove)
            set.isNotEmpty()
        }.collect()
    }

    private fun receivePartMessages(
        withLock: Boolean,
        throwOnError: Boolean,
    ): Flow<PartMessage> {
        return messageHandler
            .receiveMessages()
            .onEach { if (throwOnError) it.throwIfError() }
            .filterIsInstance<PartMessage>()
            .filter { it.user == me }
            .onEach { message ->
                if (withLock) channelsMutex.lock()
                message.channels.forEach {
                    this.channels.remove(ChannelName(it))?.cancel()
                }
                if (withLock) channelsMutex.unlock()
            }
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
                    name = ChannelName(name),
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
        return "IrkServer(hostname='$hostname', port=$port)"
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

suspend fun IrkServer.part(vararg channelNames: String) {
    partWithMessage(message = null, channelNames = channelNames.asSequence().map(::ChannelName).toSet())
}

suspend fun IrkServer.part(vararg channelNames: ChannelName) {
    partWithMessage(message = null, channelNames = channelNames.toSet())
}
