package me.evgem.irk.client.model.message

class PingMessage(
    val server1: String,
    val server2: String? = null,
    prefix: String? = null,
) : AbstractMessage(
    command = "PING",
    prefix = prefix,
    middleParams = listOfNotNull(server1, server2),
    trailingParam = null,
) {

    override fun toString(): String {
        return "PingMessage(server1='$server1', server2=$server2)"
    }
}