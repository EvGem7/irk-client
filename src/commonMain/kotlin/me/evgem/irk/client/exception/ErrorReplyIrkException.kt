package me.evgem.irk.client.exception

import me.evgem.irk.client.model.message.ReplyMessage

class ErrorReplyIrkException(
    val replyMessage: ReplyMessage,
) : IrkException(replyMessage.toString())
