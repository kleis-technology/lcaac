package ch.kleis.lcaplugin.actions.csv

import org.junit.Test
import kotlin.test.assertEquals


class CsvRequestReaderTest {
    @Test
    fun test_read_shouldReadLine() {
        // given
        val inputStream = """
            geo,id,a,b
            UK,s00,1.0,2.0
            FR,s01,2.0,1.0
        """.trimIndent().byteInputStream()
        val reader = CsvRequestReader("p", inputStream)

        // when
        val actual = reader.read()

        // then
        assertEquals(2, actual.size)
        val first = actual[0]
        assertEquals("p", first.processName)
        assertEquals("UK", first["geo"])
        assertEquals("s00", first["id"])
        assertEquals("1.0", first["a"])
        assertEquals("2.0", first["b"])
    }
}
