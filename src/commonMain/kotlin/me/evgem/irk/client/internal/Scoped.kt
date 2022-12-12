package me.evgem.irk.client.internal

import kotlinx.coroutines.CoroutineScope

interface Scoped {
    val coroutineScope: CoroutineScope
}