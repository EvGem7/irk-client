package me.evgem.irk.client.internal.di

import me.evgem.irk.client.internal.util.cast

internal abstract class AbstractComponent {

    protected val instances = mutableMapOf<String, Any>()

    protected inline fun <reified T> single(
        qualifier: String = T::class.run { qualifiedName ?: toString() },
        block: () -> T,
    ): T {
        return instances.getOrElse(qualifier) { block() }.cast()
    }

    protected inline fun <T> factory(block: () -> T): T {
        return block()
    }
}