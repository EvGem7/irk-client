package me.evgem.irk.client.test

import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import me.evgem.irk.client.model.message.ReplyMessage
import me.evgem.irk.client.model.message.UnknownMessage
import me.evgem.irk.client.model.message.misc.NumericReply
import me.evgem.irk.client.internal.network.handler.message.identifier.MessageIdentifier

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
            numericReply = NumericReply.RPL_WELCOME,
            stringReply = "Welcome",
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
            numericReply = NumericReply.ERR_ALREADYREGISTRED,
            stringReply = "ERR_ALREADYREGISTRED",
        )
        assertEquals(expected, actual)
    }
}