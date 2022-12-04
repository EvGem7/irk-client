package me.evgem.irk.client.internal.util

import kotlinx.datetime.Clock

internal object Log {

    operator fun invoke(msg: String) {
        println("${getTimestamp()}: $msg")
    }

    private fun getTimestamp(): String {
        return Clock.System.now().toString()
    }
}