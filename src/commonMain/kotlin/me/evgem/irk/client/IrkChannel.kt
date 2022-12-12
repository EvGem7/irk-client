package me.evgem.irk.client

import me.evgem.irk.client.internal.Scoped
import me.evgem.irk.client.model.User

class IrkChannel internal constructor(
    internal val server: IrkServer,
    val name: String,
    val topic: String,
    users: Set<User>,
) : Scoped by server {

    private val _users = users.toMutableSet()
    val users: Set<User> get() = _users.toSet()

    override fun toString(): String {
        return "IrkChannel(name='$name', topic='$topic')"
    }
}