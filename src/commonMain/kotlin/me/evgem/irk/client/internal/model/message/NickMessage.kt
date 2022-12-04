package me.evgem.irk.client.internal.model.message

internal class NickMessage(nickname: String) : Message(
    command = "NICK",
    trailingParam = nickname,
)
