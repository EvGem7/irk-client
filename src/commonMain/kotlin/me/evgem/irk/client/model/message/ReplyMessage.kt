package me.evgem.irk.client.model.message

import me.evgem.irk.client.exception.ErrorReplyIrkException
import me.evgem.irk.client.model.message.misc.NumericReply
import me.evgem.irk.client.model.message.misc.isError
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

    val target: String = middleParams.firstOrNull()?.toString().orEmpty()
    val replyParams: List<ByteArrayWrapper> = allParams.drop(1)
    val replyStringParams: List<String> = allStringParams.drop(1)

    override fun toString(): String {
        return "ReplyMessage(numericReply=$numericReply, allParams=$allParams)"
    }
}

fun AbstractMessage.throwIfError() {
    if (this is ReplyMessage && numericReply.isError) {
        throw ErrorReplyIrkException(
            numericReply = numericReply,
            description = replyStringParams.lastOrNull().orEmpty(),
            target = target,
        )
    }
}
