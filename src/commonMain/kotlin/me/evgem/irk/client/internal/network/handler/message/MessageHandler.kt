package me.evgem.irk.client.internal.network.handler.message

import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.isClosed
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.errors.EOFException
import io.ktor.utils.io.writeFully
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.takeWhile
import me.evgem.irk.client.exception.IrkException
import me.evgem.irk.client.internal.Closeable
import me.evgem.irk.client.internal.Scoped
import me.evgem.irk.client.internal.model.LF
import me.evgem.irk.client.internal.network.handler.message.identifier.MessageIdentifier
import me.evgem.irk.client.model.message.AbstractMessage
import me.evgem.irk.client.util.IrkLog
import me.evgem.irk.client.util.wrap

internal interface MessageHandler : Closeable, Scoped {

    suspend fun sendMessage(message: AbstractMessage)

    fun receiveMessages(): Flow<AbstractMessage>

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

        private const val MAX_MESSAGE_SIZE = 512
    }

    private sealed interface ReadState {
        class Message(val value: AbstractMessage) : ReadState
        object EOF : ReadState
        class Error(val throwable: Throwable) : ReadState
    }

    override val coroutineScope: CoroutineScope = CoroutineScope(socket.socketContext)

    private val writeChannel = socket.openWriteChannel(autoFlush = true)
    private val readChannel = socket.openReadChannel()

    private val readStateFlow: SharedFlow<ReadState> = flow {
        while (true) {
            try {
                readMessage().let {
                    if (LOG_READ) IrkLog("received $it")
                    emit(ReadState.Message(it))
                }
            } catch (e: EOFException) {
                emit(ReadState.EOF)
                close()
                break
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                emit(ReadState.Error(e))
            }
        }
    }.shareIn(coroutineScope, SharingStarted.Lazily)

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
                if (size > MAX_MESSAGE_SIZE) {
                    throw IrkException("Message size limit of $MAX_MESSAGE_SIZE exceeded.")
                }
            } while (byte != Byte.LF)
        }.readBytes()
    }

    override suspend fun sendMessage(message: AbstractMessage) {
        if (LOG_WRITE) IrkLog("sending $message")
        messageSerializer.serialize(message)
            .also { if (LOG_WRITE && LOG_RAW) IrkLog(it.toString()) }
            .let { writeChannel.writeFully(it.array) }
    }

    override fun receiveMessages(): Flow<AbstractMessage> {
        return readStateFlow
            .takeWhile { it is ReadState.Message }
            .filterIsInstance<ReadState.Message>()
            .map { it.value }
    }

    override fun close() {
        socket.socketContext.cancel()
    }

    override val isAlive: Boolean
        get() = !socket.isClosed
}
