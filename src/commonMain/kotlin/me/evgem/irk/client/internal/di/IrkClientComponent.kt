package me.evgem.irk.client.internal.di

import io.ktor.network.selector.SelectorManager
import kotlinx.coroutines.CoroutineDispatcher
import me.evgem.irk.client.internal.network.MessageHandlerFactory

internal class IrkClientComponent(
    private val workCoroutineDispatcher: CoroutineDispatcher,
) : AbstractComponent() {

    fun workCoroutineDispatcher(): CoroutineDispatcher = factory { workCoroutineDispatcher }

    fun selectorManager(): SelectorManager = single { SelectorManager(workCoroutineDispatcher()) }

    fun messageHandlerFactory(): MessageHandlerFactory = factory { MessageHandlerFactory(selectorManager()) }
}
