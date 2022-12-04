package me.evgem.irk.client

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.core.use
import io.ktor.utils.io.read
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import me.evgem.irk.client.internal.di.IrkClientComponent

class IrkClient(
    workCoroutineDispatcher: CoroutineDispatcher = Dispatchers.Default,
) {

    private val component = IrkClientComponent(workCoroutineDispatcher)

    suspend fun test() {
        println("start up")

        val selectorManager = SelectorManager()
        aSocket(selectorManager).tcp().connect("", 1).use {
            it.openWriteChannel()
            it.openReadChannel().let {
                it.readByte()
                it.read { source, start, endExclusive ->
                    source.1
                }
            }
        }
    }
}