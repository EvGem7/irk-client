package me.evgem.irk.client.test

import kotlin.test.Test
import me.evgem.irk.client.model.message.JoinMessage
import me.evgem.irk.client.model.message.NickMessage
import me.evgem.irk.client.model.message.PasswordMessage
import me.evgem.irk.client.model.message.QuitMessage
import me.evgem.irk.client.model.message.ReplyMessage
import me.evgem.irk.client.model.message.UnknownMessage
import me.evgem.irk.client.model.message.UserMessage
import me.evgem.irk.client.model.message.misc.NumericReply

class MessageConstructionTest {

    @Test
    fun `join message`() {
        JoinMessage(listOf("channel"), emptyList())
    }

    @Test
    fun `quit message`() {
        QuitMessage("")
    }

    @Test
    fun `unknown message`() {
        UnknownMessage("")
    }

    @Test
    fun `user message`() {
        UserMessage("username", 0, "real name")
    }

    @Test
    fun `password message`() {
        PasswordMessage("password")
    }

    @Test
    fun `nick message`() {
        NickMessage("nickname")
    }

    @Test
    fun `reply message`() {
        ReplyMessage(NumericReply.RPL_WELCOME)
    }
}