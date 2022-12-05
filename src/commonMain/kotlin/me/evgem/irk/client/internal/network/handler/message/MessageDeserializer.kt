package me.evgem.irk.client.internal.network.handler.message

import me.evgem.irk.client.internal.model.COLON
import me.evgem.irk.client.internal.model.CR
import me.evgem.irk.client.internal.model.MESSAGE_MAX_MIDDLE_PARAMS
import me.evgem.irk.client.internal.model.SPACE
import me.evgem.irk.client.internal.model.message.UnknownMessage
import me.evgem.irk.client.internal.util.split
import me.evgem.irk.client.util.ByteArrayWrapper

internal interface MessageDeserializer {
    fun deserialize(messageBytes: ByteArrayWrapper): UnknownMessage
}

internal fun MessageDeserializer(): MessageDeserializer = DefaultMessageDeserializer()

private class DefaultMessageDeserializer : MessageDeserializer {

    override fun deserialize(messageBytes: ByteArrayWrapper): UnknownMessage {
        var arr = messageBytes.array.run {
            val haveCR = get(lastIndex.minus(1).coerceAtLeast(0)) == Byte.CR
            copyOfRange(fromIndex = 0, toIndex = size - if (haveCR) 2 else 1)
        }
        if (arr.isEmpty()) return UnknownMessage(command = "")

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
                        fromIndex = commandEndIndex, // left 1 space so trailingColonIndex can be found
                        toIndex = arr.size,
                    )
                }
            }

        val trailingColonIndex = kotlin.run {
            var paramsCounter = 0
            for (i in 1 until arr.size) {
                val first = arr[i - 1]
                val second = arr[i]
                if (first == Byte.SPACE && second == Byte.COLON) {
                    return@run i
                }

                // to prevent finding the colon inside the trailing parameter
                if (first == Byte.SPACE && second != Byte.SPACE) {
                    if (++paramsCounter == MESSAGE_MAX_MIDDLE_PARAMS + 1) {
                        return@run -1
                    }
                }
            }
            -1
        }
        val middleParams: List<ByteArray>
        val trailingParam: ByteArray?
        if (trailingColonIndex > -1) {
            middleParams = arr.copyOfRange(0, trailingColonIndex - 1).split(Byte.SPACE)
            trailingParam = arr.copyOfRange(trailingColonIndex + 1, arr.size)
        } else {
            val split = arr.split(Byte.SPACE)
            if (split.size > MESSAGE_MAX_MIDDLE_PARAMS) {
                middleParams = split.subList(0, MESSAGE_MAX_MIDDLE_PARAMS)
                val trailingStartIndex = middleParams
                    .fold(0) { acc, param -> acc + param.size + 1 }
                    .plus(1) // 1 left space
                trailingParam = arr.copyOfRange(trailingStartIndex, arr.size)
            } else {
                middleParams = split
                trailingParam = null
            }
        }

        return UnknownMessage(
            command = command,
            prefix = prefix,
            middleParams = middleParams.map(::ByteArrayWrapper),
            trailingParam = trailingParam?.let(::ByteArrayWrapper),
        )
    }
}
