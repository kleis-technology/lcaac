package ch.kleis.lcaplugin.actions.csv

import ch.kleis.lcaplugin.core.lang.fixture.QuantityValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.UnitValueFixture
import ch.kleis.lcaplugin.core.lang.value.ProductValue
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.apache.commons.io.output.AppendableWriter
import org.junit.Test
import java.io.OutputStream
import java.io.Writer
import kotlin.test.assertEquals


class CsvResultWriterTest {

    @Test
    fun write() {
        // given
        val request = CsvRequest(
            "p",
            mapOf("id" to 0, "a" to 1, "b" to 2),
            listOf("s00", "1.0", "2.0"),
        )
        val result = CsvResult(
            request,
            ProductValue("out", UnitValueFixture.kg),
            mapOf(
                ProductValue("in1", UnitValueFixture.kg) to QuantityValueFixture.oneKilogram,
                ProductValue("in2", UnitValueFixture.l) to QuantityValueFixture.oneLitre,
            )
        )
        val outputStream = mockk<OutputStream>()
        val buffer = StringBuilder()
        val innerWriter = AppendableWriter(buffer)
        val writer = CsvResultWriter(
            outputStream,
            innerWriter,
        )

        // when
        writer.write(listOf(result))
        val actual = buffer.toString()

        // then
        val expected = """
            id, a, b, product, reference unit, in1 [kg], in2 [l]
            s00, 1.0, 2.0, out, kg, 1.0, 1.0
            
        """.trimIndent()
        assertEquals(expected, actual)
    }
}
