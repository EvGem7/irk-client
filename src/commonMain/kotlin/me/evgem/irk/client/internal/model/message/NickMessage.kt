package me.evgem.irk.client.internal.model.message

internal class NickMessage(val nickname: String) : AbstractMessage(
    command = "NICK",
    trailingParam = nickname,
) {

    override fun toString(): String {
        return "NickMessage(nickname='$nickname')"
    }
}
