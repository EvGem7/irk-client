package me.evgem.irk.client

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.plus
import me.evgem.irk.client.internal.Scoped
import me.evgem.irk.client.model.ChannelName
import me.evgem.irk.client.model.User
import me.evgem.irk.client.model.message.AbstractMessage
import me.evgem.irk.client.model.message.JoinMessage
import me.evgem.irk.client.model.message.PartMessage
import me.evgem.irk.client.model.message.QuitMessage

class IrkChannel internal constructor(
    val server: IrkServer,
    val name: ChannelName,
    val topic: String,
    users: Set<User>,
) : Scoped {

    private val _users = users.toMutableSet()
    val users: Set<User> get() = _users.toSet()

    private val job = Job(server.coroutineScope.coroutineContext[Job])
    override val coroutineScope: CoroutineScope = server.coroutineScope + job

    val messages: Flow<AbstractMessage> = server.messages.filter { message ->
        message.allStringParams.any { ChannelName(it) == name }
    }.shareIn(coroutineScope, SharingStarted.Lazily)

    init {
        observeUsers()
    }

    private fun observeUsers() {
        val quitMessages = server.messages.filter { it is QuitMessage && it.user != null }
        messages
            .filter { it.user != null && (it is JoinMessage || it is PartMessage) }
            .let { merge(it, quitMessages) }
            .onEach {
                val user = it.user!!
                _users.remove(user)
                if (it is JoinMessage) {
                    _users.add(user)
                }
            }.launchIn(coroutineScope)
    }

    suspend fun part() {
        server.part(name)
    }

    suspend fun sendMessage(text: String) {
        server.sendMessage(name.toString(), text)
    }

    internal fun cancel() {
        job.cancel()
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