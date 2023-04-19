package ch.kleis.lcaplugin.imports

import junit.framework.TestCase
import org.junit.Test

class ModelWriterTest {

    @Test
    fun write() {
        // TODO
    }

    @Test
    fun close() {
        // TODO
    }

    private data class Input(val raw: String, val expected: String)

    private data class Result(val result: String, val expected: String)

    @Test
    fun test_sanitize() {
        // Given
        val data = arrayOf(
            Input("01", "_01"),
            Input("ab", "ab"),
            Input("a_+__b", "a__p___b"),
            Input("a_*__b", "a__m___b"),
            Input("m*2a", "m_m_2a"),
            Input("a&b", "a_a_b"),
            Input("  a&b++", "a_a_b_p__p")
        )

        // When
        val result = data.map { p -> Result(ModelWriter.sanitize(p.raw), p.expected) }

        // Then
        result.forEach { r -> TestCase.assertEquals(r.expected, r.result) }
    }

    @Test
    fun test_sanitizeAndCompact() {
        // Given
        val data = arrayOf(
            Input("01", "_01"),
            Input("ab", "ab"),
            Input("a_+__b", "a_p_b"),
            Input("a_*__b", "a_m_b"),
            Input("m*2a", "m_m_2a"),
            Input("a&b", "a_a_b"),
            Input("  a&b++", "a_a_b_p_p")
        )

        // When
        val result = data.map { p -> Result(ModelWriter.sanitizeAndCompact(p.raw), p.expected) }

        // Then
        result.forEach { r -> TestCase.assertEquals(r.expected, r.result) }
    }

}