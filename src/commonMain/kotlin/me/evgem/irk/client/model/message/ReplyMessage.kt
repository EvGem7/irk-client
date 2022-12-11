package me.evgem.irk.client.model.message

import me.evgem.irk.client.exception.ErrorReplyIrkException
import me.evgem.irk.client.model.message.misc.NumericReply

class ReplyMessage internal constructor(
    val numericReply: NumericReply,
    val stringReply: String?,
) : AbstractMessage(
    command = numericReply.code.toString().let { code ->
        "0".repeat(3.minus(code.length)) + code
    },
    trailingParam = stringReply,
) {

    override fun toString(): String {
        return "ReplyMessage(numericReply=$numericReply, stringReply=$stringReply)"
    }
}

fun ReplyMessage.throwIfError() {
    if (numericReply.isError) {
        throw ErrorReplyIrkException(numericReply, stringReply)
    }
}
