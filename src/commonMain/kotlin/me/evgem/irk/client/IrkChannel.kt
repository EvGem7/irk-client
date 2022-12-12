package me.evgem.irk.client

import me.evgem.irk.client.internal.Scoped
import me.evgem.irk.client.model.User

class IrkChannel internal constructor(
    val server: IrkServer,
    val name: String,
    val topic: String,
    users: Set<User>,
) : Scoped by server {

    private val _users = users.toMutableSet()
    val users: Set<User> get() = _users.toSet()

    suspend fun part() {
        server.part(name)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is IrkChannel) return false

        if (server != other.server) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = server.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    override fun toString(): String {
        return "IrkChannel(name='$name', topic='$topic')"
    }
}