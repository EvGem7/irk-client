package me.evgem.irk.client.internal.model.message

import io.ktor.utils.io.core.toByteArray
import me.evgem.irk.client.util.ByteArrayWrapper

internal abstract class AbstractMessage(
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

    val allParams: List<ByteArrayWrapper> = middleParams + listOfNotNull(trailingParam)

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AbstractMessage) return false

        if (command != other.command) return false
        if (prefix != other.prefix) return false
        if (middleParams != other.middleParams) return false
        if (trailingParam != other.trailingParam) return false

        return true
    }

    final override fun hashCode(): Int {
        var result = command.hashCode()
        result = 31 * result + (prefix?.hashCode() ?: 0)
        result = 31 * result + middleParams.hashCode()
        result = 31 * result + (trailingParam?.hashCode() ?: 0)
        return result
    }

    abstract override fun toString(): String
}
