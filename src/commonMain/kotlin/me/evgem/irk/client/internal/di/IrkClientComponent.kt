package me.evgem.irk.client.internal.di

import io.ktor.network.selector.SelectorManager
import kotlinx.coroutines.CoroutineDispatcher
import me.evgem.irk.client.internal.network.handler.message.MessageDeserializer
import me.evgem.irk.client.internal.network.handler.message.MessageHandlerFactory
import me.evgem.irk.client.internal.network.handler.message.MessageSerializer
import me.evgem.irk.client.internal.network.handler.message.identifier.MessageFactory
import me.evgem.irk.client.internal.network.handler.message.identifier.MessageIdentifier
import me.evgem.irk.client.internal.network.handler.message.identifier.factory.JoinMessageFactory
import me.evgem.irk.client.internal.network.handler.message.identifier.factory.QuitMessageFactory

internal class IrkClientComponent(
    private val workCoroutineDispatcher: CoroutineDispatcher,
) : AbstractComponent() {

    fun workCoroutineDispatcher(): CoroutineDispatcher = factory { workCoroutineDispatcher }

    fun selectorManager(): SelectorManager = single { SelectorManager(workCoroutineDispatcher()) }

    fun messageHandlerFactory(): MessageHandlerFactory = factory {
        MessageHandlerFactory(selectorManager(), messageDeserializer(), messageSerializer(), messageIdentifier())
    }

    fun messageSerializer(): MessageSerializer = factory { MessageSerializer() }

    fun messageDeserializer(): MessageDeserializer = factory { MessageDeserializer() }

    fun messageIdentifier(): MessageIdentifier = factory { MessageIdentifier(messageFactories()) }

    fun messageFactories(): List<MessageFactory<*>> = single("message factories list") {
        listOf(
            QuitMessageFactory(),
            JoinMessageFactory(),
        )
    }
}
