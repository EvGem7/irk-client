package me.evgem.irk.client.model.message

class UserMessage internal constructor(
    val user: String,
    val mode: Int,
    val realName: String,
    val unused: String = "*",
) : AbstractMessage(
    command = "USER",
    trailingParam = realName,
    middleParams = listOf(user, mode.toString(), unused),
) {

    init {
        require(!user.contains(' '))
    }

    override fun toString(): String {
        return "UserMessage(user='$user', mode=$mode, realName='$realName', unused='$unused')"
    }
}