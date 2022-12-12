package me.evgem.irk.client.model.message

import me.evgem.irk.client.exception.ErrorReplyIrkException
import me.evgem.irk.client.model.message.misc.NumericReply
import me.evgem.irk.client.util.ByteArrayWrapper

class ReplyMessage internal constructor(
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

    override fun toString(): String {
        return "ReplyMessage(numericReply=$numericReply, allParams=$allParams)"
    }
}

fun ReplyMessage.throwIfError() {
    if (numericReply.isError) {
        throw ErrorReplyIrkException(this)
    }
}
