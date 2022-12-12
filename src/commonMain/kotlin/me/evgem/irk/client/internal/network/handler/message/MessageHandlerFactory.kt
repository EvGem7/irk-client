package me.evgem.irk.client.internal.network.handler.message

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.Socket
import io.ktor.network.sockets.TcpSocketBuilder
import io.ktor.network.sockets.aSocket
import me.evgem.irk.client.internal.network.handler.message.identifier.MessageIdentifier

internal interface MessageHandlerFactory {

    suspend fun createMessageHandler(remoteAddress: InetSocketAddress): MessageHandler

    suspend fun createMessageHandler(hostname: String, port: Int): MessageHandler
}

internal fun MessageHandlerFactory(
    selectorManager: SelectorManager,
    messageDeserializer: MessageDeserializer,
    messageSerializer: MessageSerializer,
    messageIdentifier: MessageIdentifier,
): MessageHandlerFactory {
    return DefaultMessageHandlerFactory(selectorManager, messageDeserializer, messageSerializer, messageIdentifier)
}

private class DefaultMessageHandlerFactory(
    private val selectorManager: SelectorManager,
    private val messageDeserializer: MessageDeserializer,
    private val messageSerializer: MessageSerializer,
    private val messageIdentifier: MessageIdentifier,
) : MessageHandlerFactory {

    override suspend fun createMessageHandler(remoteAddress: InetSocketAddress): MessageHandler {
        return via { connect(remoteAddress) }
    }

    override suspend fun createMessageHandler(hostname: String, port: Int): MessageHandler {
        return via { connect(hostname, port) }
    }

    private inline fun via(block: TcpSocketBuilder.() -> Socket): MessageHandler {
        return aSocket(selectorManager).tcp().block().let {
            DefaultMessageHandler(it, messageDeserializer, messageSerializer, messageIdentifier)
        }
    }
}