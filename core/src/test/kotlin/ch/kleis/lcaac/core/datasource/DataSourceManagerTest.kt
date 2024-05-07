package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.config.LcaacConnectorConfig
import ch.kleis.lcaac.core.config.LcaacDataSourceConfig
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
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class DataSourceManagerTest {
    private val connectorName = "connector"
    private val connectorConfig = LcaacConnectorConfig(name = connectorName, options = emptyMap())

    private val sourceName = "source"
    private val sourceConfig = LcaacDataSourceConfig(name = sourceName, connector = connectorName)

    private val config = LcaacConfig(
        name = "project",
        description = "description",
        datasources = mapOf(
            sourceName to sourceConfig
        ),
        connectors = mapOf(
            connectorName to connectorConfig
        ),
    )
    private val ops = BasicOperations

    @Test
    fun getAll() {
        // given
        val connector = mockk<DataSourceConnector<BasicNumber>>()
        every { connector.getAll(any(), any()) } returns sequenceOf(
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

        val sourceOps = DataSourceManager(config, ops)
        sourceOps.registerConnector(connectorName, connector)
        val source = DataSourceValue(
            name = sourceName,
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
        val actual = sourceOps.getAll(source).toList()

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
        val connector = mockk<DataSourceConnector<BasicNumber>>()
        every { connector.getFirst(any(), any()) } returns ERecord(mapOf(
            "geo" to EStringLiteral("FR"),
            "n_items" to QuantityFixture.oneUnit,
            "mass" to QuantityFixture.oneKilogram,
        ))

        val sourceOps = DataSourceManager(config, ops)
        sourceOps.registerConnector(connectorName, connector)
        val source = DataSourceValue(
            name = sourceName,
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
        val actual = sourceOps.getFirst(source)

        // then
        val expected = ERecord(mapOf(
            "geo" to EStringLiteral("FR"),
            "n_items" to QuantityFixture.oneUnit,
            "mass" to QuantityFixture.oneKilogram,
        ))
        assertEquals(expected, actual)
    }

    @Test
    fun sumProduct() {
        // given
        val connector = mockk<DataSourceConnector<BasicNumber>>()
        every { connector.getAll(any(), any()) } returns sequenceOf(
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

        val sourceOps = DataSourceManager(config, ops)
        sourceOps.registerConnector(connectorName, connector)
        val source = DataSourceValue(
            name = sourceName,
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
}
