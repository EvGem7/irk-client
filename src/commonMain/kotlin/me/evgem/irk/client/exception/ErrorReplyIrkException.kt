package me.evgem.irk.client.exception

import me.evgem.irk.client.model.message.misc.NumericReply

class ErrorReplyIrkException(
    val numericReply: NumericReply,
    val stringReply: String?,
) : IrkException(message = "code=${numericReply.code} ${stringReply.orEmpty()}")
