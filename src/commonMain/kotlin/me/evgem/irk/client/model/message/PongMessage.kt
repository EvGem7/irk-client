package me.evgem.irk.client.model.message

class PongMessage(
    val server: String,
    val server2: String? = null,
    prefix: String? = null,
) : AbstractMessage(
    command = "PONG",
    prefix = prefix,
    middleParams = listOfNotNull(server, server2),
    trailingParam = null,
) {

    override fun toString(): String {
        return "PongMessage(server='$server', server2=$server2)"
    }
}