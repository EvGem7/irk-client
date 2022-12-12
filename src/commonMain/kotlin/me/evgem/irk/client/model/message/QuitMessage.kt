package me.evgem.irk.client.model.message

class QuitMessage internal constructor(
    val quitMessage: String,
    prefix: String? = null,
) : AbstractMessage(
    command = "QUIT",
    trailingParam = quitMessage,
    prefix = prefix,
) {

    override fun toString(): String {
        return "QuitMessage(quitMessage='$quitMessage', user=$user)"
    }
}