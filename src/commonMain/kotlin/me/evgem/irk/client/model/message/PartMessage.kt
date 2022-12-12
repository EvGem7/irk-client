package me.evgem.irk.client.model.message

class PartMessage(
    val channels: List<String>,
    val partMessage: String?,
    prefix: String? = null,
) : AbstractMessage(
    command = "PART",
    trailingParam = partMessage,
    middleParams = listOf(
        channels.joinToString(separator = ","),
    ),
    prefix = prefix,
) {

    override fun toString(): String {
        return "PartMessage(channels=$channels, partMessage=$partMessage, user=$user)"
    }
}
