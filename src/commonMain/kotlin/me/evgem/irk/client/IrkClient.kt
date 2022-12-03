package me.evgem.irk.client

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.utils.io.readUTF8Line

object IrkClient {

    suspend fun test() {
        println("start up")

        val selectorManager = SelectorManager()
        val serverSocket = aSocket(selectorManager).tcp().bind("127.0.0.1", 9003)
        serverSocket.accept().openReadChannel().readUTF8Line().let(::println)
    }
}