package me.evgem.irk.client.internal.util

fun ByteArray.split(byte: Byte): List<ByteArray> {
    var start = 0
    val result = mutableListOf<ByteArray>()
    for (i in indices) {
        if (get(i) == byte) {
            if (start < i) {
                result += copyOfRange(fromIndex = start, toIndex = i)
            }
            start = i + 1
        }
    }
    if (start < size) {
        result += copyOfRange(fromIndex = start, toIndex = size)
    }
    return result
}
