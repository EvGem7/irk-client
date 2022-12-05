package me.evgem.irk.client.internal.model.message

import me.evgem.irk.client.internal.model.message.misc.NumericReply

internal class ReplyMessage(
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
