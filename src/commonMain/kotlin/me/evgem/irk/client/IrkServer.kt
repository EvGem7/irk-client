package me.evgem.irk.client

import me.evgem.irk.client.internal.network.handler.message.MessageHandler

class IrkServer internal constructor(
    private val messageHandler: MessageHandler,
    val welcomeMessage: String,
    motd: String,
) {

    var motd: String = motd
        private set
}