package me.evgem.irk.client.internal.model.message

internal class PasswordMessage(val password: String) : AbstractMessage(
    command = "PASS",
    trailingParam = password,
) {

    override fun toString(): String {
        return "PasswordMessage(password='$password')"
    }
}
