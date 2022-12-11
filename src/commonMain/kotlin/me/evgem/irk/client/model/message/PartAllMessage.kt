package me.evgem.irk.client.model.message

object PartAllMessage : AbstractMessage(
    command = "JOIN",
    trailingParam = null,
    middleParams = listOf("0"),
) {

    override fun toString(): String {
        return "PartAllMessage"
    }
}