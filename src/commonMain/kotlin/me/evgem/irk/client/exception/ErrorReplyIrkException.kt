package me.evgem.irk.client.exception

import me.evgem.irk.client.model.message.misc.NumericReply

class ErrorReplyIrkException(
    val numericReply: NumericReply,
    val target: String,
    val description: String,
) : IrkException("reply=$numericReply, target=$target, description=$description")
