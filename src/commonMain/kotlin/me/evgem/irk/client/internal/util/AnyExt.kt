package me.evgem.irk.client.internal.util

internal inline fun <T> T?.orElse(block: () -> T): T {
    return this ?: block()
}

internal inline fun <T> T?.orDefault(default: T): T {
    return this ?: default
}

internal inline fun <reified T> Any?.cast(): T = this as T

internal inline fun <reified T> Any?.castOrNull(): T? = this as? T
