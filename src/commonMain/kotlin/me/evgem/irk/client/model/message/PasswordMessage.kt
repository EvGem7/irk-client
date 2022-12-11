package me.evgem.irk.client.model.message

class PasswordMessage internal constructor(val password: String) : AbstractMessage(
    command = "PASS",
    trailingParam = password,
) {

    override fun toString(): String {
        return "PasswordMessage(password='$password')"
    }
}
