package me.evgem.irk.client.internal.network.handler.message

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.SocketAddress
import io.ktor.network.sockets.TcpSocketBuilder
import io.ktor.network.sockets.aSocket

internal interface MessageHandlerFactory {

    suspend fun createMessageHandler(remoteAddress: SocketAddress): MessageHandler

    suspend fun createMessageHandler(hostname: String, port: Int): MessageHandler
}

internal fun MessageHandlerFactory(
    selectorManager: SelectorManager,
    messageDeserializer: MessageDeserializer,
    messageSerializer: MessageSerializer,
): MessageHandlerFactory {
    return DefaultMessageHandlerFactory(selectorManager, messageDeserializer, messageSerializer)
}

private class DefaultMessageHandlerFactory(
    private val selectorManager: SelectorManager,
    private val messageDeserializer: MessageDeserializer,
    private val messageSerializer: MessageSerializer,
) : MessageHandlerFactory {

    override suspend fun createMessageHandler(remoteAddress: SocketAddress): MessageHandler {
        return via { connect(remoteAddress) }
    }

    override suspend fun createMessageHandler(hostname: String, port: Int): MessageHandler {
        return via { connect(hostname, port) }
    }

    private inline fun via(block: TcpSocketBuilder.() -> Socket): MessageHandler {
        return aSocket(selectorManager).tcp().block().let {
            DefaultMessageHandler(it, messageDeserializer, messageSerializer)
        }
    }
}