package me.evgem.irk.client.internal.model

/**
 * Carriage return
 */
val Byte.Companion.CR: Byte get() = 0x0D.toByte()

/**
 * Line feed
 */
val Byte.Companion.LF: Byte get() = 0x0A.toByte()

val Byte.Companion.Colon: Byte get() = ':'.code.toByte()

val Byte.Companion.Space: Byte get() = ' '.code.toByte()
