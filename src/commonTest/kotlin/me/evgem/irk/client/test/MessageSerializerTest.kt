package me.evgem.irk.client.test

import io.ktor.utils.io.core.toByteArray
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import me.evgem.irk.client.internal.model.message.Message
import me.evgem.irk.client.internal.network.handler.message.MessageSerializer
import me.evgem.irk.client.util.wrap

class MessageSerializerTest {

    private lateinit var serializer: MessageSerializer

    @BeforeTest
    fun init() {
        serializer = MessageSerializer()
    }

    @Test
    fun `test prefix present`() {
        val message = Message(
            command = "123",
            prefix = "some_prefix",
        )
        val actual = serializer.serialize(message)
        val expected = ":some_prefix 123\r\n".toByteArray().wrap()
        assertEquals(expected, actual)
    }

    @Test
    fun `test prefix absent`() {
        val message = Message(
            command = "123",
        )
        val actual = serializer.serialize(message)
        val expected = "123\r\n".toByteArray().wrap()
        assertEquals(expected, actual)
    }

    @Test
    fun `test command absent`() {
        assertFails {
            val message = Message(
                command = "",
            )
            serializer.serialize(message)
        }
    }

    @Test
    fun `test command with spaces`() {
        assertFails {
            val message = Message(
                command = "123 321",
            )
            serializer.serialize(message)
        }
    }

    @Test
    fun `test command with cr`() {
        assertFails {
            val message = Message(
                command = "123\r321",
            )
            serializer.serialize(message)
        }
    }

    @Test
    fun `test command with lf`() {
        assertFails {
            val message = Message(
                command = "123\n321",
                middleParams = listOf("m1\nand\nm2"),
                trailingParam = null,
            )
            serializer.serialize(message)
        }
    }

    @Test
    fun `test command with colon`() {
        assertFails {
            val message = Message(
                command = "123:",
            )
            serializer.serialize(message)
        }
    }

    @Test
    fun `test command with null`() {
        assertFails {
            val message = Message(
                command = "123\u0000321",
            )
            serializer.serialize(message)
        }
    }

    @Test
    fun `test 1 middle param`() {
        val message = Message(
            command = "123",
            middleParams = listOf("m1"),
            trailingParam = null,
        )
        val actual = serializer.serialize(message)
        val expected = "123 m1\r\n".toByteArray().wrap()
        assertEquals(expected, actual)
    }

    @Test
    fun `test 5 middle params`() {
        val message = Message(
            command = "123",
            middleParams = (1..5).map { "m$it" },
            trailingParam = null,
        )
        val actual = serializer.serialize(message)
        val expected = "123 m1 m2 m3 m4 m5\r\n".toByteArray().wrap()
        assertEquals(expected, actual)
    }

    @Test
    fun `test 14 middle params`() {
        val message = Message(
            command = "123",
            middleParams = (1..14).map { "m$it" },
            trailingParam = null,
        )
        val actual = serializer.serialize(message)
        val expected = "123 m1 m2 m3 m4 m5 m6 m7 m8 m9 m10 m11 m12 m13 m14\r\n".toByteArray().wrap()
        assertEquals(expected, actual)
    }

    @Test
    fun `test 15 middle params`() {
        assertFails {
            val message = Message(
                command = "123",
                middleParams = (1..15).map { "m$it" },
                trailingParam = null,
            )
            serializer.serialize(message)
        }
    }

    @Test
    fun `test middle param with spaces`() {
        assertFails {
            val message = Message(
                command = "123",
                middleParams = listOf("m1 and m2"),
                trailingParam = null,
            )
            serializer.serialize(message)
        }
    }

    @Test
    fun `test middle param with cr`() {
        assertFails {
            val message = Message(
                command = "123",
                middleParams = listOf("m1\rand\rm2"),
                trailingParam = null,
            )
            serializer.serialize(message)
        }
    }

    @Test
    fun `test middle param with lf`() {
        assertFails {
            val message = Message(
                command = "123",
                middleParams = listOf("m1\nand\nm2"),
                trailingParam = null,
            )
            serializer.serialize(message)
        }
    }

    @Test
    fun `test middle param with colon not at start`() {
        val message = Message(
            command = "123",
            middleParams = listOf("m1:m2:"),
            trailingParam = null,
        )
        val actual = serializer.serialize(message)
        val expected = "123 m1:m2:\r\n".toByteArray().wrap()
        assertEquals(expected, actual)
    }

    @Test
    fun `test middle param with colon at start`() {
        assertFails {
            val message = Message(
                command = "123",
                middleParams = listOf(":m1"),
                trailingParam = null,
            )
            serializer.serialize(message)
        }
    }

    @Test
    fun `test middle param with null`() {
        assertFails {
            val message = Message(
                command = "123",
                middleParams = listOf("m1\u0000m2"),
                trailingParam = null,
            )
            serializer.serialize(message)
        }
    }

    @Test
    fun `test empty middle param`() {
        val message = Message(
            command = "123",
            middleParams = listOf(
                "m1".toByteArray().wrap(),
                ByteArray(0).wrap(),
                "m2".toByteArray().wrap(),
            ),
        )
        val actual = serializer.serialize(message)
        val expected = "123 m1 m2\r\n".toByteArray().wrap()
        assertEquals(expected, actual)
    }

    @Test
    fun `test trailingParam present`() {
        val message = Message(
            command = "123",
            trailingParam = "yep",
        )
        val actual = serializer.serialize(message)
        val expected = "123 :yep\r\n".toByteArray().wrap()
        assertEquals(expected, actual)
    }

    @Test
    fun `test trailingParam present with spaces`() {
        val message = Message(
            command = "123",
            trailingParam = "yep cock туды сюды",
        )
        val actual = serializer.serialize(message)
        val expected = "123 :yep cock туды сюды\r\n".toByteArray().wrap()
        assertEquals(expected, actual)
    }

    @Test
    fun `test trailingParam and 5 middle params`() {
        val message = Message(
            command = "123",
            trailingParam = "yep cock туды сюды",
            middleParams = (1..5).map { "m$it" },
        )
        val actual = serializer.serialize(message)
        val expected = "123 m1 m2 m3 m4 m5 :yep cock туды сюды\r\n".toByteArray().wrap()
        assertEquals(expected, actual)
    }

    @Test
    fun `test trailingParam and 5 middle params with colons`() {
        val message = Message(
            command = "123",
            trailingParam = "yep cock туды сюды",
            middleParams = (1..5).map { "m:$it:" },
        )
        val actual = serializer.serialize(message)
        val expected = "123 m:1: m:2: m:3: m:4: m:5: :yep cock туды сюды\r\n".toByteArray().wrap()
        assertEquals(expected, actual)
    }

    @Test
    fun `test trailingParam and 14 middle params`() {
        val message = Message(
            command = "123",
            trailingParam = "yep cock туды сюды",
            middleParams = (1..14).map { "m$it" },
        )
        val actual = serializer.serialize(message)
        val expected = "123 m1 m2 m3 m4 m5 m6 m7 m8 m9 m10 m11 m12 m13 m14 yep cock туды сюды\r\n".toByteArray().wrap()
        assertEquals(expected, actual)
    }

    @Test
    fun `test trailingParam present with colon at start`() {
        val message = Message(
            command = "123",
            trailingParam = ":::yep cock : :туды сюды:",
        )
        val actual = serializer.serialize(message)
        val expected = "123 ::::yep cock : :туды сюды:\r\n".toByteArray().wrap()
        assertEquals(expected, actual)
    }

    @Test
    fun `test trailingParam with colon at start and 14 middle params`() {
        val message = Message(
            command = "123",
            trailingParam = ":::yep cock : :туды сюды:",
            middleParams = (1..14).map { "m$it" },
        )
        val actual = serializer.serialize(message)
        val expected =
            "123 m1 m2 m3 m4 m5 m6 m7 m8 m9 m10 m11 m12 m13 m14 :::yep cock : :туды сюды:\r\n".toByteArray().wrap()
        assertEquals(expected, actual)
    }

    @Test
    fun `test trailing param with cr`() {
        assertFails {
            val message = Message(
                command = "123",
                trailingParam = "m1\rand\rm2",
            )
            serializer.serialize(message)
        }
    }

    @Test
    fun `test trailing param with lf`() {
        assertFails {
            val message = Message(
                command = "123",
                trailingParam = "m1\nand\nm2",
            )
            serializer.serialize(message)
        }
    }

    @Test
    fun `test trailing param with null`() {
        assertFails {
            val message = Message(
                command = "123",
                trailingParam = "m1\u0000m2",
            )
            serializer.serialize(message)
        }
    }

    @Test
    fun `test empty trailing param`() {
        val message = Message(
            command = "123",
            trailingParam = "",
        )
        val actual = serializer.serialize(message)
        val expected = "123 :\r\n".toByteArray().wrap()
        assertEquals(expected, actual)
    }
}