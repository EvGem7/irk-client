package me.evgem.irk.client.internal.model.message

internal class QuitMessage(val quitMessage: String) : AbstractMessage(
    command = "QUIT",
    trailingParam = quitMessage,
) {

    override fun toString(): String {
        return "QuitMessage(quitMessage='$quitMessage')"
    }
}