package me.evgem.irk.client.internal.model

/**
 * Carriage return
 */
val Byte.Companion.CR: Byte get() = 0x0D.toByte()

/**
 * Line feed
 */
val Byte.Companion.LF: Byte get() = 0x0A.toByte()

val Byte.Companion.COLON: Byte get() = ':'.code.toByte()
val Byte.Companion.SPACE: Byte get() = ' '.code.toByte()
val Byte.Companion.NULL: Byte get() = 0.toByte()

const val MESSAGE_MAX_MIDDLE_PARAMS = 14
