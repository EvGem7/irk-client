package me.evgem.irk.client.model.message

class QuitMessage internal constructor(
    val quitMessage: String,
    val who: String? = null,
) : AbstractMessage(
    command = "QUIT",
    trailingParam = quitMessage,
    prefix = who,
) {

    override fun toString(): String {
        return "QuitMessage(quitMessage='$quitMessage', who=$who)"
    }
}