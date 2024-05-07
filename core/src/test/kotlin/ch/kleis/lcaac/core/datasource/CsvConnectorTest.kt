package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.config.LcaacDataSourceConfig
import ch.kleis.lcaac.core.datasource.csv.CsvConnector
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

class CsvConnectorTest {
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
    fun getAll() {
        // given
        val connector = CsvConnector(
            mockk(),
            ops,
            mockFileLoader(),
        )
        val source = DataSourceValue(
            name = "source",
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
        val config = LcaacDataSourceConfig(
            name = "source",
            location = "source.csv",
        )

        // when
        val actual = connector.getAll(config, source).toList()

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
    fun getFirst() {
        // given
        val connector = CsvConnector(
            mockk(),
            ops,
            mockFileLoader(),
        )
        val source = DataSourceValue(
            name = "source",
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
        val config = LcaacDataSourceConfig(
            name = "source",
            location = "source.csv",
        )

        // when
        val actual = connector.getFirst(config, source)

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
        val connector = CsvConnector(
            mockk(),
            ops,
            mockFileLoader(),
        )
        val source = DataSourceValue(
            name = "source",
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
        val config = LcaacDataSourceConfig(
            name = "source",
            location = "source.csv",
        )

        // when/then
        val e = assertThrows<EvaluatorException> { connector.getFirst(config, source) }
        assertEquals("no record found in 'source.csv' matching {geo=A_NON_EXISTING_COUNTRY}", e.message)
    }
}
