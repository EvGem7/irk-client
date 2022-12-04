package me.evgem.irk.client.internal.model.message

import io.ktor.utils.io.core.toByteArray
import me.evgem.irk.client.util.ByteArrayWrapper

internal open class Message(
    val command: String,
    val prefix: String? = null,
    val middleParams: List<ByteArrayWrapper> = emptyList(),
    val trailingParam: ByteArrayWrapper? = null,
) {

    constructor(
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Message) return false

        if (command != other.command) return false
        if (prefix != other.prefix) return false
        if (middleParams != other.middleParams) return false
        if (trailingParam != other.trailingParam) return false

        return true
    }

    override fun hashCode(): Int {
        var result = command.hashCode()
        result = 31 * result + (prefix?.hashCode() ?: 0)
        result = 31 * result + middleParams.hashCode()
        result = 31 * result + (trailingParam?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "Message(command='$command', prefix=$prefix, middleParams=$middleParams, trailingParam=$trailingParam)"
    }
}
