package me.evgem.irk.client.exception

class LoginIrkException(
    message: String? = null,
    val welcomeMessage: String? = null,
    val motd: String? = null,
) : IrkException(message) {

    override fun toString(): String {
        return "LoginIrkException(message=$message, welcomeMessage=$welcomeMessage, motd=$motd)"
    }
}
