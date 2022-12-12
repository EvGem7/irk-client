package me.evgem.irk.client.test

import io.ktor.utils.io.core.toByteArray
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import me.evgem.irk.client.internal.network.handler.message.identifier.MessageIdentifier
import me.evgem.irk.client.model.message.ReplyMessage
import me.evgem.irk.client.model.message.UnknownMessage
import me.evgem.irk.client.model.message.misc.KnownNumericReply
import me.evgem.irk.client.util.wrap

class MessageIdentifierTest {

    private lateinit var identifier: MessageIdentifier

    @BeforeTest
    fun init() {
        identifier = MessageIdentifier(emptyList())
    }

    @Test
    fun `test reply message with leading zeros`() {
        val unknownMessage = UnknownMessage(
            command = "001",
            trailingParam = "Welcome",
        )
        val actual = identifier.identify(unknownMessage)
        val expected = ReplyMessage(
            numericReply = KnownNumericReply.RPL_WELCOME,
            trailingParam = "Welcome".toByteArray().wrap(),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test reply message without leading zeros`() {
        val unknownMessage = UnknownMessage(
            command = "462",
            trailingParam = "ERR_ALREADYREGISTRED",
        )
        val actual = identifier.identify(unknownMessage)
        val expected = ReplyMessage(
            numericReply = KnownNumericReply.ERR_ALREADYREGISTRED,
            trailingParam = "ERR_ALREADYREGISTRED".toByteArray().wrap(),
        )
        assertEquals(expected, actual)
    }
}