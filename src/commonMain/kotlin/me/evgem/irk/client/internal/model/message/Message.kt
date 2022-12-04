package me.evgem.irk.client.internal.model.message

import me.evgem.irk.client.internal.util.ByteArrayWrapper

internal open class Message(
    val command: String,
    val prefix: String? = null,
    val middleParams: List<ByteArrayWrapper> = emptyList(),
    val trailingParam: ByteArrayWrapper? = null,
) {

    companion object {
        const val MAX_MIDDLE_PARAMS = 14
    }

    init {
        require(middleParams.size <= MAX_MIDDLE_PARAMS)
    }

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
