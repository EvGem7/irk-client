package me.evgem.irk.client.test

import io.ktor.utils.io.core.toByteArray
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import me.evgem.irk.client.internal.model.message.AbstractMessage
import me.evgem.irk.client.internal.model.message.EmptyMessage
import me.evgem.irk.client.internal.model.message.UnknownMessage
import me.evgem.irk.client.internal.network.handler.message.MessageDeserializer
import me.evgem.irk.client.util.wrap

class MessageDeserializerTest {

    private lateinit var deserializer: MessageDeserializer

    @BeforeTest
    fun init() {
        deserializer = MessageDeserializer()
    }

    @Test
    fun `test prefix present`() {
        val msg = ":a_prefix command\r\n"
        val actual = deserializer.deserialize(msg.toByteArray().wrap())
        val expected = UnknownMessage(
            command = "command",
            prefix = "a_prefix",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test prefix absent`() {
        val msg = "command\r\n"
        val actual = deserializer.deserialize(msg.toByteArray().wrap())
        val expected = UnknownMessage(
            command = "command",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test no cr`() {
        val msg = "command\n"
        val actual = deserializer.deserialize(msg.toByteArray().wrap())
        val expected = UnknownMessage(
            command = "command",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test 1 space in the end`() {
        val msg = "command \n"
        val actual = deserializer.deserialize(msg.toByteArray().wrap())
        val expected = UnknownMessage(
            command = "command",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test multiple spaces in the end`() {
        val msg = "command     \n"
        val actual = deserializer.deserialize(msg.toByteArray().wrap())
        val expected = UnknownMessage(
            command = "command",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test 1 middle param`() {
        val msg = "command p1\n"
        val actual = deserializer.deserialize(msg.toByteArray().wrap())
        val expected = UnknownMessage(
            command = "command",
            middleParams = listOf("p1"),
            trailingParam = null,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test 1 middle param with colons`() {
        val msg = "command p:1:::\n"
        val actual = deserializer.deserialize(msg.toByteArray().wrap())
        val expected = UnknownMessage(
            command = "command",
            middleParams = listOf("p:1:::"),
            trailingParam = null,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test 1 middle param with colons and trailing param`() {
        val msg = "command p:1::: :a trailing param\n"
        val actual = deserializer.deserialize(msg.toByteArray().wrap())
        val expected = UnknownMessage(
            command = "command",
            middleParams = listOf("p:1:::"),
            trailingParam = "a trailing param",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test 1 middle param with colons and trailing param with colons`() {
        val msg = "command p:1::: ::a :fdf t:::railing param:::\n"
        val actual = deserializer.deserialize(msg.toByteArray().wrap())
        val expected = UnknownMessage(
            command = "command",
            middleParams = listOf("p:1:::"),
            trailingParam = ":a :fdf t:::railing param:::",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test 5 middle params`() {
        val msg = "command p:1::: p:2::: p3 p4 p5:\n"
        val actual = deserializer.deserialize(msg.toByteArray().wrap())
        val expected = UnknownMessage(
            command = "command",
            middleParams = listOf(
                "p:1:::",
                "p:2:::",
                "p3",
                "p4",
                "p5:",
            ),
            trailingParam = null,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test 14 middle params`() {
        val params = (1..14).joinToString(separator = "") { " p$it" }
        val msg = "command$params\n"
        val actual = deserializer.deserialize(msg.toByteArray().wrap())
        val expected = UnknownMessage(
            command = "command",
            middleParams = (1..14).map { "p$it" },
            trailingParam = null,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test 14 middle params and trailing param without delimiter`() {
        val params = (1..14).joinToString(separator = "") { " p$it" }
        val msg = "command$params a trailing mf\n"
        val actual = deserializer.deserialize(msg.toByteArray().wrap())
        val expected = UnknownMessage(
            command = "command",
            middleParams = (1..14).map { "p$it" },
            trailingParam = "a trailing mf",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test 14 middle params and trailing param with delimiter`() {
        val params = (1..14).joinToString(separator = "") { " p$it" }
        val msg = "command$params :a trailing mf\n"
        val actual = deserializer.deserialize(msg.toByteArray().wrap())
        val expected = UnknownMessage(
            command = "command",
            middleParams = (1..14).map { "p$it" },
            trailingParam = "a trailing mf",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test 14 middle params and trailing param without delimiter and with colons`() {
        val params = (1..14).joinToString(separator = "") { " p$it" }
        val msg = "command$params a: :trailing: :mf:\n"
        val actual = deserializer.deserialize(msg.toByteArray().wrap())
        val expected = UnknownMessage(
            command = "command",
            middleParams = (1..14).map { "p$it" },
            trailingParam = "a: :trailing: :mf:",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test 14 middle params and trailing param with delimiter and with colons`() {
        val params = (1..14).joinToString(separator = "") { " p$it" }
        val msg = "command$params :a: :trailing: :mf:\n"
        val actual = deserializer.deserialize(msg.toByteArray().wrap())
        val expected = UnknownMessage(
            command = "command",
            middleParams = (1..14).map { "p$it" },
            trailingParam = "a: :trailing: :mf:",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test prefix with trailing param`() {
        val msg = ":prefix.example.com a_command :some :message:\r\n"
        val actual = deserializer.deserialize(msg.toByteArray().wrap())
        val expected = UnknownMessage(
            command = "a_command",
            trailingParam = "some :message:",
            prefix = "prefix.example.com",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test prefix empty`() {
        val msg = ": a_command\r\n"
        val actual = deserializer.deserialize(msg.toByteArray().wrap())
        val expected = UnknownMessage(
            command = "a_command",
            prefix = "",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test message empty`() {
        val msg = "\r\n"
        val actual: AbstractMessage = deserializer.deserialize(msg.toByteArray().wrap())
        val expected: AbstractMessage = EmptyMessage
        assertEquals(expected, actual)
    }

    @Test
    @Ignore // such msg doesn't satisfy the standard
    fun `test too much spaces`() {
        val msg = ":prefix    command   p1     :trailing\r\n"
        val actual = deserializer.deserialize(msg.toByteArray().wrap())
        val expected = UnknownMessage(
            command = "command",
            prefix = "prefix",
            middleParams = listOf("p1"),
            trailingParam = "trailing",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test trailing param empty`() {
        val msg = ":prefix command p1 :\r\n"
        val actual = deserializer.deserialize(msg.toByteArray().wrap())
        val expected = UnknownMessage(
            command = "command",
            prefix = "prefix",
            middleParams = listOf("p1"),
            trailingParam = "",
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test trailing param has 14 words`() {
        val trailing = (1..14).joinToString(separator = " ") { "$it" }
        val msg = "command :$trailing\r\n"
        val actual = deserializer.deserialize(msg.toByteArray().wrap())
        val expected = UnknownMessage(
            command = "command",
            trailingParam = trailing,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test trailing param has 15 words`() {
        val trailing = (1..15).joinToString(separator = " ") { "$it" }
        val msg = "command :$trailing\r\n"
        val actual = deserializer.deserialize(msg.toByteArray().wrap())
        val expected = UnknownMessage(
            command = "command",
            trailingParam = trailing,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test trailing param has 16 words`() {
        val trailing = (1..16).joinToString(separator = " ") { "$it" }
        val msg = "command :$trailing\r\n"
        val actual = deserializer.deserialize(msg.toByteArray().wrap())
        val expected = UnknownMessage(
            command = "command",
            trailingParam = trailing,
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test trailing param has 14 words with 1 middle param`() {
        val trailing = (1..14).joinToString(separator = " ") { "$it" }
        val msg = "command p1 :$trailing\r\n"
        val actual = deserializer.deserialize(msg.toByteArray().wrap())
        val expected = UnknownMessage(
            command = "command",
            trailingParam = trailing,
            middleParams = listOf("p1"),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun `test trailing param has 15 words with 1 middle param`() {
        val trailing = (1..15).joinToString(separator = " ") { "$it" }
        val msg = "command p1 :$trailing\r\n"
        val actual = deserializer.deserialize(msg.toByteArray().wrap())
        val expected = UnknownMessage(
            command = "command",
            trailingParam = trailing,
            middleParams = listOf("p1"),
        )
        assertEquals(expected, actual)
    }
}