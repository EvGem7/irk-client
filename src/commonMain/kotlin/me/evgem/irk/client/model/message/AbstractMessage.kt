package me.evgem.irk.client.model.message

import io.ktor.utils.io.core.toByteArray
import me.evgem.irk.client.model.User
import me.evgem.irk.client.util.ByteArrayWrapper

abstract class AbstractMessage internal constructor(
    internal val command: String,
    internal val prefix: String? = null,
    internal val middleParams: List<ByteArrayWrapper> = emptyList(),
    internal val trailingParam: ByteArrayWrapper? = null,
) {

    internal constructor(
        command: String,
        trailingParam: String?,
        prefix: String? = null,
        middleParams: List<String> = emptyList(),
    ) : this(
        command = command,
        prefix = prefix,
        middleParams = middleParams.map { ByteArrayWrapper(it.toByteArray()) },
        trailingParam = trailingParam?.toByteArray()?.let(::ByteArrayWrapper),
    )

    internal val allParams: List<ByteArrayWrapper> = middleParams + listOfNotNull(trailingParam)
    internal val allStringParams: List<String> = allParams.map(ByteArrayWrapper::toString)

    val user: User? = prefix
        ?.takeIf { it.isNotBlank() }
        ?.indexOfFirst { it == '!' || it == '@' }
        ?.takeIf { it >= 0 }
        ?.let { prefix.substring(0, it) }
        ?.let(User::fromNick)

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractMessage) return false

        if (command != other.command) return false
        if (prefix != other.prefix) return false
        if (allParams != other.allParams) return false

        return true
    }

    final override fun hashCode(): Int {
        var result = command.hashCode()
        result = 31 * result + (prefix?.hashCode() ?: 0)
        result = 31 * result + allParams.hashCode()
        return result
    }

    abstract override fun toString(): String
}
