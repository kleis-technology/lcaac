package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.expression.EStringLiteral
import ch.kleis.lcaac.core.lang.expression.EUnitLiteral
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture
import ch.kleis.lcaac.core.lang.fixture.QuantityValueFixture
import ch.kleis.lcaac.core.lang.fixture.UnitFixture
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.InputStream
import kotlin.test.assertEquals

class CsvSourceOperationsTest {
    private fun mockFileLoader(): (String) -> InputStream {
        val content = """
            geo,n_items,mass
            FR,1,1
            FR,2,2
            FR,1,2
            UK,2,2
            UK,1,2
            UK,2,1
        """.trimIndent()
        return { content.byteInputStream() }
    }

    private val ops = BasicOperations

    @Test
    fun readAll() {
        // given
        val sourceOps = CsvSourceOperations(
            mockk(),
            ops,
            mockFileLoader(),
        )
        val source = DataSourceValue(
            location = "source.csv",
            schema = mapOf(
                "geo" to StringValue("FR"),
                "n_items" to QuantityValueFixture.oneUnit,
                "mass" to QuantityValueFixture.oneKilogram,
            ),
            filter = mapOf(
                "geo" to StringValue("FR")
            )
        )

        // when
        val actual = sourceOps.readAll(source).toList()

        // then
        val expected = listOf(
            ERecord(mapOf(
                "geo" to EStringLiteral("FR"),
                "n_items" to QuantityFixture.oneUnit,
                "mass" to QuantityFixture.oneKilogram,
            )),
            ERecord(mapOf(
                "geo" to EStringLiteral("FR"),
                "n_items" to QuantityFixture.twoUnits,
                "mass" to QuantityFixture.twoKilograms,
            )),
            ERecord(mapOf(
                "geo" to EStringLiteral("FR"),
                "n_items" to QuantityFixture.oneUnit,
                "mass" to QuantityFixture.twoKilograms,
            )),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun sumProduct() {
        // given
        val sourceOps = CsvSourceOperations(
            mockk(),
            ops,
            mockFileLoader(),
        )
        val source = DataSourceValue(
            location = "source.csv",
            schema = mapOf(
                "geo" to StringValue("FR"),
                "n_items" to QuantityValueFixture.oneUnit,
                "mass" to QuantityValueFixture.oneKilogram,
            ),
            filter = mapOf(
                "geo" to StringValue("FR")
            )
        )

        // when
        val actual = sourceOps.sumProduct(source, listOf("n_items", "mass"))

        // then
        val expected = EQuantityScale(
            BasicNumber(7.0),
            EUnitLiteral(
                UnitFixture.kg.symbol.multiply(UnitFixture.unit.symbol),
                1.0,
                UnitFixture.kg.dimension.multiply(UnitFixture.unit.dimension),
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun getFirst() {
        // given
        val sourceOps = CsvSourceOperations(
            mockk(),
            ops,
            mockFileLoader(),
        )
        val source = DataSourceValue(
            location = "source.csv",
            schema = mapOf(
                "geo" to StringValue("FR"),
                "n_items" to QuantityValueFixture.oneUnit,
                "mass" to QuantityValueFixture.oneKilogram,
            ),
            filter = mapOf(
                "geo" to StringValue("UK")
            )
        )

        // when
        val actual = sourceOps.getFirst(source)

        // then
        val expected = ERecord(mapOf(
            "geo" to EStringLiteral("UK"),
            "n_items" to QuantityFixture.twoUnits,
            "mass" to QuantityFixture.twoKilograms,
        ))
        assertEquals(expected, actual)
    }

    @Test
    fun getFirst_whenEmpty_throwEvaluatorException() {
        // given
        val sourceOps = CsvSourceOperations(
            mockk(),
            ops,
            mockFileLoader(),
        )
        val source = DataSourceValue(
            location = "source.csv",
            schema = mapOf(
                "geo" to StringValue("FR"),
                "n_items" to QuantityValueFixture.oneUnit,
                "mass" to QuantityValueFixture.oneKilogram,
            ),
            filter = mapOf(
                "geo" to StringValue("A_NON_EXISTING_COUNTRY")
            )
        )

        // when/then
        val e = assertThrows<EvaluatorException> { sourceOps.getFirst(source) }
        assertEquals("no record found in 'source.csv' matching {geo=A_NON_EXISTING_COUNTRY}", e.message)
    }
}
