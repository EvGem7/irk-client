package me.evgem.irk.client.internal.network.handler.message

import me.evgem.irk.client.internal.model.CR
import me.evgem.irk.client.internal.model.COLON
import me.evgem.irk.client.internal.model.MESSAGE_MAX_MIDDLE_PARAMS
import me.evgem.irk.client.internal.model.SPACE
import me.evgem.irk.client.internal.model.message.Message
import me.evgem.irk.client.util.ByteArrayWrapper
import me.evgem.irk.client.internal.util.split

internal interface MessageDeserializer {
    fun deserialize(messageBytes: ByteArrayWrapper): Message
}

internal fun MessageDeserializer(): MessageDeserializer = DefaultMessageDeserializer()

private class DefaultMessageDeserializer : MessageDeserializer {

    override fun deserialize(messageBytes: ByteArrayWrapper): Message {
        var arr = messageBytes.array.run {
            val haveCR = get(lastIndex.minus(1).coerceAtLeast(0)) == Byte.CR
            copyOfRange(fromIndex = 0, toIndex = size - if (haveCR) 2 else 1)
        }

        val prefix: String?
        if (arr[0] == Byte.COLON) {
            val spaceIndex = arr.indexOf(Byte.SPACE).also {
                require(it > 0) { "Invalid prefix: ${arr.decodeToString()}" }
            }
            prefix = arr.decodeToString(startIndex = 1, endIndex = spaceIndex)
            arr = arr.copyOfRange(fromIndex = spaceIndex + 1, toIndex = arr.size)
        } else {
            prefix = null
        }

        val command = arr
            .indexOfFirst { it == Byte.SPACE }
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
                if (first == Byte.SPACE && second == Byte.COLON) {
                    return@run i
                }
            }
            -1
        }
        val middleParams: List<ByteArray>
        val trailingParam: ByteArray?
        if (trailingColonIndex > -1) {
            middleParams = arr.copyOfRange(0, trailingColonIndex - 1).split(Byte.SPACE)

            // TODO make a test to handle the case when trailing param is present, but it is empty
            trailingParam = arr.copyOfRange(trailingColonIndex + 1, arr.size)
        } else {
            val split = arr.split(Byte.SPACE)
            if (split.size > MESSAGE_MAX_MIDDLE_PARAMS) {
                middleParams = split.subList(0, MESSAGE_MAX_MIDDLE_PARAMS)
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
}
