package me.evgem.irk.client.util

class ByteArrayWrapper(val array: ByteArray) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as ByteArrayWrapper

        if (!array.contentEquals(other.array)) return false

        return true
    }

    override fun hashCode(): Int {
        return array.contentHashCode()
    }

    override fun toString(): String {
        return array.decodeToString()
    }
}

fun ByteArray.wrap(): ByteArrayWrapper = ByteArrayWrapper(this)
