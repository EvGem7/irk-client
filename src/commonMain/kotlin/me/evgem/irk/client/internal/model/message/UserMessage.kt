package me.evgem.irk.client.internal.model.message

internal class UserMessage(
    user: String,
    mode: Int,
    realName: String,
    unused: String = "*",
) : Message(
    command = "USER",
    trailingParam = realName,
    middleParams = listOf(user, mode.toString(), unused),
) {

    init {
        require(!user.contains(' '))
    }
}