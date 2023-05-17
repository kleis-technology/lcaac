package ch.kleis.lcaplugin.actions.csv

import ch.kleis.lcaplugin.core.lang.fixture.QuantityValueFixture
import ch.kleis.lcaplugin.core.lang.fixture.UnitValueFixture
import ch.kleis.lcaplugin.core.lang.value.ProductValue
import io.mockk.mockk
import org.apache.commons.io.output.AppendableWriter
import org.junit.Test
import java.io.OutputStream
import kotlin.test.assertEquals


class CsvResultWriterTest {

    @Test
    fun write() {
        // given
        val request = CsvRequest(
            "p",
            mapOf("comment" to 0, "id" to 1, "a" to 2, "b" to 3),
            listOf("""Comment, with comma, and "double quotes" """, "s00", "1.0", "2.0"),
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
            comment,id,a,b,product,reference unit,in1 [kg],in2 [l]
            "Comment, with comma, and ""double quotes"" ",s00,1.0,2.0,out,kg,1.0,1.0
            
        """.trimIndent()
        assertEquals(expected, actual)
    }
}
