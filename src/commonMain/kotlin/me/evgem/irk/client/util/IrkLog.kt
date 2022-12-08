package me.evgem.irk.client.util

import kotlinx.datetime.Clock

object IrkLog {

    interface Printer {
        fun print(msg: String)
    }

    object StdoutPrinter : Printer {

        override fun print(msg: String) = println("${getTimestamp()}: $msg")

        private fun getTimestamp(): String {
            return Clock.System.now().toString()
        }
    }

    val printers = mutableListOf<Printer>()

    operator fun invoke(msg: String) {
        printers.forEach {
            it.print(msg)
        }
    }
}