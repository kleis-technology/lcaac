package ch.kleis.lcaac.core.datasource.in_memory

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.expression.EStringLiteral
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture
import ch.kleis.lcaac.core.lang.fixture.QuantityValueFixture
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.math.basic.BasicOperations
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class InMemoryConnectorTest {
    private val ops = BasicOperations

    @Test
    fun getAll() {
        // given
        val schema = mapOf(
            "geo" to StringValue("FR"),
            "n_items" to QuantityValueFixture.oneUnit,
            "mass" to QuantityValueFixture.oneKilogram,
        )
        val inMemoryDatasource = InMemoryDatasource(
            records = listOf(
                mapOf(
                    "geo" to InMemStr("FR"),
                    "n_items" to InMemNum(1.0),
                    "mass" to InMemNum(1.0),
                ),
                mapOf(
                    "geo" to InMemStr("FR"),
                    "n_items" to InMemNum(2.0),
                    "mass" to InMemNum(2.0),
                ),
                mapOf(
                    "geo" to InMemStr("FR"),
                    "n_items" to InMemNum(1.0),
                    "mass" to InMemNum(2.0),
                ),
            )
        )
        val connector = InMemoryConnector(
            config = InMemoryConnectorKeys.defaultConfig(),
            content = mapOf(
                "inventory" to inMemoryDatasource
            ),
            ops = ops,
        )
        val config = DataSourceConfig(
            name = "inventory",
            connector = InMemoryConnectorKeys.IN_MEMORY_CONNECTOR_NAME,
        )
        val source = DataSourceValue(
            config = DataSourceConfig(
                name = "source",
            ),
            schema = schema,
            filter = mapOf(
                "geo" to StringValue("FR")
            )
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
        val schema = mapOf(
            "geo" to StringValue("FR"),
            "n_items" to QuantityValueFixture.oneUnit,
            "mass" to QuantityValueFixture.oneKilogram,
        )
        val inMemoryDatasource = InMemoryDatasource(
            records = listOf(
                mapOf(
                    "geo" to InMemStr("EN"),
                    "n_items" to InMemNum(1.0),
                    "mass" to InMemNum(1.0),
                ),
                mapOf(
                    "geo" to InMemStr("FR"),
                    "n_items" to InMemNum(2.0),
                    "mass" to InMemNum(2.0),
                ),
                mapOf(
                    "geo" to InMemStr("FR"),
                    "n_items" to InMemNum(1.0),
                    "mass" to InMemNum(2.0),
                ),
            )
        )
        val connector = InMemoryConnector(
            config = InMemoryConnectorKeys.defaultConfig(),
            content = mapOf(
                "inventory" to inMemoryDatasource
            ),
            ops = ops,
        )
        val config = DataSourceConfig(
            name = "inventory",
            connector = InMemoryConnectorKeys.IN_MEMORY_CONNECTOR_NAME,
        )
        val source = DataSourceValue(
            config = DataSourceConfig(
                name = "source",
            ),
            schema = schema,
            filter = mapOf(
                "geo" to StringValue("FR")
            )
        )

        // when
        val actual = connector.getFirst(config, source)

        // then
        val expected = ERecord(mapOf(
            "geo" to EStringLiteral("FR"),
            "n_items" to QuantityFixture.twoUnits,
            "mass" to QuantityFixture.twoKilograms,
        ))
        assertEquals(expected, actual)
    }

    @Test
    fun getFirst_whenEmpty_throwEvaluatorException() {
        // given
        val schema = mapOf(
            "geo" to StringValue("FR"),
            "n_items" to QuantityValueFixture.oneUnit,
            "mass" to QuantityValueFixture.oneKilogram,
        )
        val inMemoryDatasource = InMemoryDatasource(
            records = emptyList(),
        )
        val connector = InMemoryConnector(
            config = InMemoryConnectorKeys.defaultConfig(),
            content = mapOf(
                "inventory" to inMemoryDatasource,
            ),
            ops = ops,
        )
        val config = DataSourceConfig(
            name = "inventory",
            connector = InMemoryConnectorKeys.IN_MEMORY_CONNECTOR_NAME,
        )
        val source = DataSourceValue(
            config = DataSourceConfig(
                name = "source",
            ),
            schema = schema,
            filter = mapOf(
                "geo" to StringValue("A_NON_EXISTING_COUNTRY")
            )
        )

        // when/then
        val e = assertThrows<EvaluatorException> { connector.getFirst(config, source) }
        assertEquals("no record found in datasource 'inventory' matching {geo=A_NON_EXISTING_COUNTRY}", e.message)
    }
}
