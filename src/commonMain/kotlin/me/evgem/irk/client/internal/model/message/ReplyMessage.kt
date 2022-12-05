package me.evgem.irk.client.internal.model.message

import me.evgem.irk.client.internal.model.message.misc.NumericReply
import me.evgem.irk.client.util.ByteArrayWrapper

internal class ReplyMessage(
    val numericReply: NumericReply,
    prefix: String? = null,
    middleParams: List<ByteArrayWrapper> = emptyList(),
    trailingParam: ByteArrayWrapper? = null,
) : AbstractMessage(
    command = numericReply.code.toString().let { code ->
        "0".repeat(3.minus(code.length)) + code
    },
    prefix = prefix,
    middleParams = middleParams,
    trailingParam = trailingParam,
) {

    val stringReply: String? = trailingParam?.toString()

    override fun toString(): String {
        return "ReplyMessage(numericReply=$numericReply, stringReply=$stringReply)"
    }
}
