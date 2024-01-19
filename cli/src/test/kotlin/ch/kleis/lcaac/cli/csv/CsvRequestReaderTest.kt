package ch.kleis.lcaac.cli.csv

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse


class CsvRequestReaderTest {
    @Test
    fun iterator() {
        // given
        val content = """
            country,year,fossil,nuclear,hydro
            ch,2020,0.20,0.40,0.40
        """.trimIndent()
        val reader = CsvRequestReader(
                "process",
                mapOf("label" to "value"),
                content.byteInputStream(),
                mapOf(
                        "year" to "2024",
                        "foo" to "bar",
                )
        )

        // when
        val iterator = reader.iterator()
        val actual = iterator.next()

        // then
        assertEquals(actual.processName, "process")
        assertEquals(actual.matchLabels, mapOf("label" to "value"))
        assertEquals(actual.columns(), listOf("country", "year", "fossil", "nuclear", "hydro", "foo"))
        assertEquals(actual.arguments(), listOf("ch", "2024", "0.20", "0.40", "0.40", "bar"))
        assertFalse { iterator.hasNext() }
    }
}
