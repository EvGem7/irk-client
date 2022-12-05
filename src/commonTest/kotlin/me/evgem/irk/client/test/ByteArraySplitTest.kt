package me.evgem.irk.client.test

import io.ktor.utils.io.core.toByteArray
import kotlin.test.Test
import kotlin.test.assertEquals
import me.evgem.irk.client.internal.model.SPACE
import me.evgem.irk.client.internal.util.split

class ByteArraySplitTest {

    @Test
    fun `test split simple`() {
        testSplit(
            input = "yep cock туды сюды",
            expected = listOf(
                "yep",
                "cock",
                "туды",
                "сюды",
            ),
        )
    }

    @Test
    fun `test split multiple spaces`() {
        testSplit(
            input = "yep    cock   туды         сюды",
            expected = listOf(
                "yep",
                "cock",
                "туды",
                "сюды",
            ),
        )
    }

    @Test
    fun `test split multiple spaces at the start and the end`() {
        testSplit(
            input = "    yep    cock   туды         сюды  ",
            expected = listOf(
                "yep",
                "cock",
                "туды",
                "сюды",
            ),
        )
    }

    @Test
    fun `test split 1 element`() {
        testSplit(
            input = "hello",
            expected = listOf(
                "hello",
            ),
        )
    }

    @Test
    fun `test split 1 element not trimmed`() {
        testSplit(
            input = "  hello   ",
            expected = listOf(
                "hello",
            ),
        )
    }

    private fun testSplit(
        splitBy: Byte = Byte.SPACE,
        input: String,
        expected: List<String>,
    ) {
        assertEquals(
            expected = expected,
            actual = input.toByteArray().split(splitBy).map { it.decodeToString() },
        )
    }
}