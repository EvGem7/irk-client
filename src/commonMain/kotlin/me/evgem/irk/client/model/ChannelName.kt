package me.evgem.irk.client.model

class ChannelName(name: String) {

    val value: String
    val prefix: String

    init {
        when (name.firstOrNull()) {
            '&', '#', '+', '!' -> {
                this.value = name.drop(1)
                this.prefix = name.substring(0, 1)
            }

            else -> {
                this.value = name
                this.prefix = ""
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChannelName) return false

        if (value != other.value) return false

        return true
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    override fun toString(): String {
        return prefix + value
    }
}
