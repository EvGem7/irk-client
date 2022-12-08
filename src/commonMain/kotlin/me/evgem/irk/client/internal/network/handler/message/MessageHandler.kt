package me.evgem.irk.client.internal.network.handler.message

import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.isClosed
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.shareIn
import me.evgem.irk.client.internal.model.LF
import me.evgem.irk.client.internal.model.message.AbstractMessage
import me.evgem.irk.client.internal.network.handler.message.identifier.MessageIdentifier
import me.evgem.irk.client.internal.util.Closeable
import me.evgem.irk.client.util.IrkLog
import me.evgem.irk.client.util.wrap

internal interface MessageHandler : Closeable {

    suspend fun sendMessage(message: AbstractMessage)

    fun receiveMessages(): SharedFlow<AbstractMessage>

    val isAlive: Boolean
}

internal class DefaultMessageHandler(
    private val socket: Socket,
    private val messageDeserializer: MessageDeserializer,
    private val messageSerializer: MessageSerializer,
    private val messageIdentifier: MessageIdentifier,
) : MessageHandler {

    companion object {
        private const val LOG_RAW = false
        private const val LOG_READ = true
        private const val LOG_WRITE = true
    }

    private val writeChannel = socket.openWriteChannel(autoFlush = true)
    private val readChannel = socket.openReadChannel()

    private val receiveMessagesFlow: SharedFlow<AbstractMessage> = flow {
        while (true) {
            readMessage().let {
                if (LOG_READ) IrkLog("received $it")
                emit(it)
            }
        }
    }.shareIn(CoroutineScope(socket.socketContext), SharingStarted.Lazily)

    private suspend fun readMessage(): AbstractMessage {
        return readMessageByteArray()
            .also { if (LOG_READ && LOG_RAW) IrkLog("received raw: ${it.decodeToString()}") }
            .wrap()
            .let(messageDeserializer::deserialize)
            .let(messageIdentifier::identify)
    }

    private suspend fun readMessageByteArray(): ByteArray {
        return buildPacket {
            do {
                val byte = readChannel.readByte()
                writeByte(byte)
            } while (byte != Byte.LF)
        }.readBytes()
    }

    override suspend fun sendMessage(message: AbstractMessage) {
        if (LOG_WRITE) IrkLog("sending $message")
        messageSerializer.serialize(message)
            .also { if (LOG_WRITE && LOG_RAW) IrkLog(it.toString()) }
            .let { writeChannel.writeFully(it.array) }
    }

    override fun receiveMessages(): SharedFlow<AbstractMessage> {
        return receiveMessagesFlow
    }

    override fun close() {
        socket.close()
    }

    override val isAlive: Boolean
        get() = !socket.isClosed
}
