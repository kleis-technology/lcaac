package ch.kleis.lcaac.core.datasource.in_memory

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.expression.EStringLiteral
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture
import ch.kleis.lcaac.core.lang.fixture.QuantityValueFixture
import ch.kleis.lcaac.core.lang.fixture.UnitValueFixture
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class InMemoryConnectorTest {
    private fun str(s: String): DataValue<BasicNumber> = StringValue(s)
    private fun numU(value: Double): DataValue<BasicNumber> = QuantityValue(
        BasicNumber(value),
        UnitValueFixture.unit(),
    )
    private fun numKg(value: Double): DataValue<BasicNumber> = QuantityValue(
        BasicNumber(value),
        UnitValueFixture.kg(),
    )

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
                    "geo" to str("FR"),
                    "n_items" to numU(1.0),
                    "mass" to numKg(1.0),
                ),
                mapOf(
                    "geo" to str("FR"),
                    "n_items" to numU(2.0),
                    "mass" to numKg(2.0),
                ),
                mapOf(
                    "geo" to str("FR"),
                    "n_items" to numU(1.0),
                    "mass" to numKg(2.0),
                ),
            ).map { RecordValue(it) }
        )
        val connector = InMemoryConnector(
            config = InMemoryConnectorKeys.defaultConfig(),
            content = mapOf(
                "inventory" to inMemoryDatasource
            ),
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
                    "geo" to str("EN"),
                    "n_items" to numU(1.0),
                    "mass" to numKg(1.0),
                ),
                mapOf(
                    "geo" to str("FR"),
                    "n_items" to numU(2.0),
                    "mass" to numKg(2.0),
                ),
                mapOf(
                    "geo" to str("FR"),
                    "n_items" to numU(1.0),
                    "mass" to numKg(2.0),
                ),
            ).map { RecordValue(it) }
        )
        val connector = InMemoryConnector(
            config = InMemoryConnectorKeys.defaultConfig(),
            content = mapOf(
                "inventory" to inMemoryDatasource
            ),
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
        val inMemoryDatasource = InMemoryDatasource<BasicNumber>(
            records = emptyList(),
        )
        val connector = InMemoryConnector(
            config = InMemoryConnectorKeys.defaultConfig(),
            content = mapOf(
                "inventory" to inMemoryDatasource,
            ),
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
