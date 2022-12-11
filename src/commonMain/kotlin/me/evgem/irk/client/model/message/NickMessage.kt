package me.evgem.irk.client.model.message

class NickMessage internal constructor(val nickname: String) : AbstractMessage(
    command = "NICK",
    trailingParam = nickname,
) {

    override fun toString(): String {
        return "NickMessage(nickname='$nickname')"
    }
}
