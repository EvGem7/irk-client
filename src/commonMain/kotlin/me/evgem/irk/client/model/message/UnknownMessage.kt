package me.evgem.irk.client.model.message

import io.ktor.utils.io.core.toByteArray
import me.evgem.irk.client.util.ByteArrayWrapper

class UnknownMessage internal constructor(
    command: String,
    prefix: String? = null,
    middleParams: List<ByteArrayWrapper> = emptyList(),
    trailingParam: ByteArrayWrapper? = null,
) : AbstractMessage(
    command = command,
    prefix = prefix,
    middleParams = middleParams,
    trailingParam = trailingParam,
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

    override fun toString(): String {
        return "UnknownMessage(command='$command', prefix=$prefix, middleParams=$middleParams, trailingParam=$trailingParam)"
    }
}
