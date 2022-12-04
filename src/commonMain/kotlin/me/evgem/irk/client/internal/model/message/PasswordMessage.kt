package me.evgem.irk.client.internal.model.message

internal class PasswordMessage(password: String) : Message(
    command = "PASS",
    trailingParam = password,
)
