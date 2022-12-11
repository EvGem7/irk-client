package me.evgem.irk.client

class IrkChannel internal constructor(
    internal val server: IrkServer,
    val name: String,
    val topic: String,
) {
}