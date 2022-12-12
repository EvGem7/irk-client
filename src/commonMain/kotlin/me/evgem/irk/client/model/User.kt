package me.evgem.irk.client.model

class User private constructor(
    val nick: String,
    val prefix: String,
) {

    companion object {

        internal fun fromNameReply(nameReply: String): User {
            val firstChar = nameReply.first()
            val hasPrefix = firstChar == '@' || firstChar == '+'
            return User(
                nick = if (hasPrefix) nameReply.drop(1) else nameReply,
                prefix = if (hasPrefix) nameReply.substring(0, 1) else ""
            )
        }

        internal fun fromNick(nick: String): User = User(nick = nick, prefix = "")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is User) return false

        if (nick != other.nick) return false

        return true
    }

    override fun hashCode(): Int {
        return nick.hashCode()
    }

    override fun toString(): String {
        return prefix + nick
    }
}
