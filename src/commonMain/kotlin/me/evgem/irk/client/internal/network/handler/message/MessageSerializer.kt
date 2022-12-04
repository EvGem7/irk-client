package me.evgem.irk.client.internal.network.handler.message

import io.ktor.utils.io.core.buildPacket
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.core.writeFully
import me.evgem.irk.client.internal.model.CR
import me.evgem.irk.client.internal.model.LF
import me.evgem.irk.client.internal.model.message.Message
import me.evgem.irk.client.internal.util.ByteArrayWrapper
import me.evgem.irk.client.internal.util.wrap

internal interface MessageSerializer {
    fun serialize(message: Message): ByteArrayWrapper
}

internal fun MessageSerializer(): MessageSerializer = DefaultMessageSerializer()

private class DefaultMessageSerializer : MessageSerializer {

    override fun serialize(message: Message): ByteArrayWrapper {
        return buildPacket {
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
        }.readBytes().wrap()
    }
}
