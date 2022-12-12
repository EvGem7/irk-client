package me.evgem.irk.client.model.message

class UserMessage internal constructor(
    val username: String,
    val mode: Int,
    val realName: String,
    val unused: String = "*",
) : AbstractMessage(
    command = "USER",
    trailingParam = realName,
    middleParams = listOf(username, mode.toString(), unused),
) {

    init {
        require(!username.contains(' '))
    }

    override fun toString(): String {
        return "UserMessage(user='$username', mode=$mode, realName='$realName', unused='$unused')"
    }
}