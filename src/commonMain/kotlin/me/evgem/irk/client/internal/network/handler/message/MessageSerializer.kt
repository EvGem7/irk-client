package me.evgem.irk.client.internal.network.handler.message

import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.core.toByteArray
import io.ktor.utils.io.core.writeFully
import me.evgem.irk.client.internal.model.COLON
import me.evgem.irk.client.internal.model.CR
import me.evgem.irk.client.internal.model.LF
import me.evgem.irk.client.internal.model.MESSAGE_MAX_MIDDLE_PARAMS
import me.evgem.irk.client.internal.model.NULL
import me.evgem.irk.client.internal.model.SPACE
import me.evgem.irk.client.internal.model.message.AbstractMessage
import me.evgem.irk.client.util.ByteArrayWrapper
import me.evgem.irk.client.util.wrap

internal interface MessageSerializer {
    fun serialize(message: AbstractMessage): ByteArrayWrapper
}

internal fun MessageSerializer(): MessageSerializer = DefaultMessageSerializer()

private class DefaultMessageSerializer : MessageSerializer {

    override fun serialize(message: AbstractMessage): ByteArrayWrapper {
        verify(message)
        return buildPacket {
            message.prefix?.let {
                append(':')
                append(it)
                append(' ')
            }
            append(message.command)
            message
                .middleParams
                .asSequence()
                .map { it.array }
                .filter { it.isNotEmpty() }
                .forEach {
                    append(' ')
                    writeFully(it)
                }
            message.trailingParam?.let {
                append(' ')
                if (message.middleParams.size < MESSAGE_MAX_MIDDLE_PARAMS) {
                    append(':')
                }
                writeFully(it.array)
            }
            writeByte(Byte.CR)
            writeByte(Byte.LF)
        }.readBytes().wrap()
    }

    private fun verify(message: AbstractMessage) {
        message.middleParams.forEach { verifyParam(it.array) }
        message.trailingParam?.array?.let(this::verifyParam)
        verifyParam(message.command.toByteArray())
        if (message.command.contains(' ')) {
            throw IllegalArgumentException("Command cannot contain spaces.")
        }
        if (message.command.contains(':')) {
            throw IllegalArgumentException("Command cannot contain colons.")
        }
        if (message.middleParams.any { it.array.contains(Byte.SPACE) }) {
            throw IllegalArgumentException("Middle parameters cannot contain spaces.")
        }
        if (message.middleParams.any { it.array.firstOrNull() == Byte.COLON }) {
            throw IllegalArgumentException("Middle parameters cannot start with colon \':\'.")
        }
        if (message.middleParams.size > MESSAGE_MAX_MIDDLE_PARAMS) {
            throw IllegalArgumentException("Maximum $MESSAGE_MAX_MIDDLE_PARAMS middle params is allowed.")
        }
        if (message.command.isBlank()) {
            throw IllegalArgumentException("Message command cannot be blank.")
        }
    }

    private fun verifyParam(param: ByteArray) {
        when {
            param.contains(Byte.CR) -> throw IllegalArgumentException("Parameter cannot contain CR bytes.")
            param.contains(Byte.LF) -> throw IllegalArgumentException("Parameter cannot contain LF bytes.")
            param.contains(Byte.NULL) -> throw IllegalArgumentException("Parameter cannot contain NULL bytes.")
        }
    }
}
