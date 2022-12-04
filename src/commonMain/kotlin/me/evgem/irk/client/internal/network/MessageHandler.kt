package me.evgem.irk.client.internal.network

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.SocketAddress
import io.ktor.network.sockets.TcpSocketBuilder
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.isClosed
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.core.Closeable
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.core.writeFully
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import me.evgem.irk.client.internal.model.CR
import me.evgem.irk.client.internal.model.Colon
import me.evgem.irk.client.internal.model.LF
import me.evgem.irk.client.internal.model.Space
import me.evgem.irk.client.internal.model.message.Message
import me.evgem.irk.client.internal.util.ByteArrayWrapper
import me.evgem.irk.client.internal.util.Log
import me.evgem.irk.client.internal.util.split

internal interface MessageHandler : Closeable {

    suspend fun sendMessage(message: Message)

    fun receiveMessages(): SharedFlow<Message>

    val isAlive: Boolean
}

internal interface MessageHandlerFactory {

    suspend fun createMessageHandler(remoteAddress: SocketAddress): MessageHandler

    suspend fun createMessageHandler(hostname: String, port: Int): MessageHandler
}

internal fun MessageHandlerFactory(selectorManager: SelectorManager): MessageHandlerFactory {
    return DefaultMessageHandlerFactory(selectorManager)
}

private class DefaultMessageHandlerFactory(
    private val selectorManager: SelectorManager,
) : MessageHandlerFactory {

    override suspend fun createMessageHandler(remoteAddress: SocketAddress): MessageHandler {
        return via { connect(remoteAddress) }
    }

    override suspend fun createMessageHandler(hostname: String, port: Int): MessageHandler {
        return via { connect(hostname, port) }
    }

    private inline fun via(block: TcpSocketBuilder.() -> Socket): MessageHandler {
        return aSocket(selectorManager).tcp().block().let(::DefaultMessageHandler)
    }
}

private class DefaultMessageHandler(
    private val socket: Socket,
) : MessageHandler {

    private val writeChannel = socket.openWriteChannel(autoFlush = true)
    private val readChannel = socket.openReadChannel()

    private val receiveMessagesFlow: SharedFlow<Message> = flow {
        while (true) {
            readMessage().let {
                Log("received $it")
                emit(it)
            }
        }
    }.shareIn(CoroutineScope(socket.socketContext), SharingStarted.Lazily)

    private suspend fun readMessage(): Message {
        var arr = readMessageByteArray()
//            .also { Log("received raw: ${it.decodeToString()}") }
            .run {
                val haveCR = get(lastIndex.minus(1).coerceAtLeast(0)) == Byte.CR
                copyOfRange(fromIndex = 0, toIndex = size - if (haveCR) 2 else 1)
            }

        val prefix: String?
        if (arr[0] == Byte.Colon) {
            val spaceIndex = arr.indexOf(Byte.Space).also {
                require(it > 0) { "Invalid prefix: ${arr.decodeToString()}" }
            }
            prefix = arr.decodeToString(startIndex = 1, endIndex = spaceIndex)
            arr = arr.copyOfRange(fromIndex = spaceIndex + 1, toIndex = arr.size)
        } else {
            prefix = null
        }

        val command = arr
            .indexOfFirst { it == Byte.Space }
            .let { if (it == -1) arr.size else it }
            .let { commandEndIndex ->
                arr.decodeToString(startIndex = 0, endIndex = commandEndIndex).also {
                    arr = arr.copyOfRange(
                        fromIndex = commandEndIndex,
                        toIndex = arr.size,
                    )
                }
            }

        val trailingColonIndex = kotlin.run {
            for (i in 1 until arr.size) {
                val first = arr[i - 1]
                val second = arr[i]
                if (first == Byte.Space && second == Byte.Colon) {
                    return@run i
                }
            }
            -1
        }
        val middleParams: List<ByteArray>
        val trailingParam: ByteArray?
        if (trailingColonIndex > -1) {
            middleParams = arr.copyOfRange(0, trailingColonIndex - 1).split(Byte.Space)

            // TODO make a test to handle the case when trailing param is present, but it is empty
            trailingParam = arr.copyOfRange(trailingColonIndex + 1, arr.size)
        } else {
            val split = arr.split(Byte.Space)
            if (split.size > Message.MAX_MIDDLE_PARAMS) {
                middleParams = split.subList(0, Message.MAX_MIDDLE_PARAMS)
                val trailingStartIndex = middleParams.fold(0) { acc, param -> acc + param.size + 1 }
                trailingParam = arr.copyOfRange(trailingStartIndex, arr.size)
            } else {
                middleParams = split
                trailingParam = null
            }
        }

        return Message(
            command = command,
            prefix = prefix,
            middleParams = middleParams.map(::ByteArrayWrapper),
            trailingParam = trailingParam?.let(::ByteArrayWrapper),
        )
    }

    private suspend fun readMessageByteArray(): ByteArray {
        return buildPacket {
            do {
                val byte = readChannel.readByte()
                writeByte(byte)
            } while (byte != Byte.LF)
        }.readBytes()
    }

    override suspend fun sendMessage(message: Message) {
        Log("sending $message")
        buildPacket {
            message.prefix?.let {
                append(':')
                append(it)
                append(' ')
            }
            append(message.command)
            message.middleParams.forEach {
                append(' ')
                writeFully(it.array)
            }
            message.trailingParam?.let {
                append(' ')
                if (message.middleParams.size < Message.MAX_MIDDLE_PARAMS) {
                    append(':')
                }
                writeFully(it.array)
            }
            writeByte(Byte.CR)
            writeByte(Byte.LF)
        }
//            .also { Log("sending raw: ${it.copy().readText()}") }
            .let { writeChannel.writePacket(it) }
    }

    override fun receiveMessages(): SharedFlow<Message> {
        return receiveMessagesFlow
    }

    override fun close() {
        socket.close()
    }

    override val isAlive: Boolean
        get() = !socket.isClosed
}
