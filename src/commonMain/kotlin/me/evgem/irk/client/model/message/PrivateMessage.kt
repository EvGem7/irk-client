package me.evgem.irk.client.model.message

class PrivateMessage(
    val target: String,
    val text: String,
    prefix: String? = null,
) : AbstractMessage(
    command = "PRIVMSG",
    trailingParam = text,
    middleParams = listOf(target),
    prefix = prefix,
) {

    override fun toString(): String {
        return "PrivateMessage(target='$target', text='$text')"
    }
}
