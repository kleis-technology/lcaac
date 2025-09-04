package ch.kleis.lcaac.core.datasource

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.datasource.csv.CsvConnectorKeys
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryConnector
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryConnectorKeys
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryDatasource
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.expression.EStringLiteral
import ch.kleis.lcaac.core.lang.expression.EUnitLiteral
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture
import ch.kleis.lcaac.core.lang.fixture.QuantityValueFixture
import ch.kleis.lcaac.core.lang.fixture.UnitFixture
import ch.kleis.lcaac.core.lang.fixture.UnitValueFixture
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultDataSourceOperationsTest {
    private val connectorName = "connector"
    private val connectorConfig = ConnectorConfig(name = connectorName, options = emptyMap())

    private val sourceName = "source"

    private val symbolTable = SymbolTable.empty<BasicNumber>()

    private val config = LcaacConfig(
        name = "project",
        description = "description",
        connectors = listOf(connectorConfig),
    )
    private val ops = BasicOperations

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
        val connector = mockk<DataSourceConnector<BasicNumber>>()
        every { connector.getName() } returns connectorName
        every { connector.getConfig() } returns ConnectorConfig(
            name = connectorName,
            options = emptyMap(),
        )
        every { connector.getAll(any(), any(), any()) } returns sequenceOf(
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
        val builder = mockk<ConnectorBuilder<BasicNumber>>()
        every { builder.buildOrNull(any(), any()) } returns connector
        val factory = ConnectorFactory(".", config, ops, symbolTable, listOf(builder))
        val sourceOps = DefaultDataSourceOperations(ops, factory.buildConnectors(), emptyMap())
        val source = DataSourceValue(
            config = DataSourceConfig(
                name = sourceName,
                connector = connectorName,
            ),
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
        every { connector.getName() } returns connectorName
        every { connector.getConfig() } returns ConnectorConfig(
            name = connectorName,
            options = emptyMap(),
        )
        every { connector.getFirst(any(), any(), any()) } returns ERecord(mapOf(
            "geo" to EStringLiteral("FR"),
            "n_items" to QuantityFixture.oneUnit,
            "mass" to QuantityFixture.oneKilogram,
        ))

        val builder = mockk<ConnectorBuilder<BasicNumber>>()
        every { builder.buildOrNull(any(), any()) } returns connector
        val factory = ConnectorFactory(".", config, ops, symbolTable, listOf(builder))
        val sourceOps = DefaultDataSourceOperations(
            ops,

            factory.buildConnectors(),
            emptyMap()
        )
        val source = DataSourceValue(
            config = DataSourceConfig(
                name = sourceName,
                connector = connectorName,
            ),
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
    fun sumProduct_whenEmptySequence() {
        // given
        val connector = mockk<DataSourceConnector<BasicNumber>>()
        every { connector.getName() } returns connectorName
        every { connector.getAll(any(), any(), any()) } returns sequenceOf()
        every { connector.getConfig() } returns ConnectorConfig(
            name = connectorName,
            options = emptyMap(),
        )

        val builder = mockk<ConnectorBuilder<BasicNumber>>()
        every { builder.buildOrNull(any(), any()) } returns connector
        val factory = ConnectorFactory(".", config, ops, symbolTable, listOf(builder))
        val sourceOps = DefaultDataSourceOperations(
            ops,

            factory.buildConnectors(),
            emptyMap()
        )
        val source = DataSourceValue(
            config = DataSourceConfig(
                name = sourceName,
                connector = connectorName,
            ),
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
            BasicNumber(0.0),
            UnitFixture.unit.times(UnitFixture.kg),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun sumProduct_wheNonEmptySequence() {
        // given
        val connector = mockk<DataSourceConnector<BasicNumber>>()
        every { connector.getName() } returns connectorName
        every { connector.getConfig() } returns ConnectorConfig(
            name = connectorName,
            options = emptyMap(),
        )
        every { connector.getAll(any(), any(), any()) } returns sequenceOf(
            ERecord(mapOf(
                "geo" to EStringLiteral("FR"),
                "n_items" to QuantityFixture.oneUnit,
                "mass" to QuantityFixture.oneKilogram,
            )),
            ERecord(mapOf(
                "geo" to EStringLiteral("FR"),
                "n_items" to QuantityFixture.oneUnit,
                "mass" to QuantityFixture.twoKilograms,
            )),
        )

        val builder = mockk<ConnectorBuilder<BasicNumber>>()
        every { builder.buildOrNull(any(), any()) } returns connector
        val factory = ConnectorFactory(".", config, ops, symbolTable, listOf(builder))
        val sourceOps = DefaultDataSourceOperations(
            ops,

            factory.buildConnectors(),
            emptyMap()
        )
        val source = DataSourceValue(
            config = DataSourceConfig(
                name = sourceName,
                connector = connectorName,
            ),
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
            BasicNumber(3.0),
            UnitFixture.unit.times(UnitFixture.kg),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun getAll_override_whenMatchOverride() {
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
                    "geo" to str("UK"),
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
        val inMemoryConnector = InMemoryConnector(
            config = InMemoryConnectorKeys.defaultConfig(),
            content = mapOf(
                "inventory" to inMemoryDatasource,
            ),
        )
        val sourceOps = DefaultDataSourceOperations(
            BasicOperations,
            connectors = mapOf(inMemoryConnector.getName() to inMemoryConnector),
        )
        val source = DataSourceValue(
            config = DataSourceConfig(
                name = "inventory",
                connector = "in_memory",
            ),
            schema = schema,
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
                "n_items" to QuantityFixture.oneUnit,
                "mass" to QuantityFixture.twoKilograms,
            )),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun getAll_override_whenNoMatchOverride() {
        // given
        val schema = mapOf(
            "geo" to StringValue("FR"),
            "n_items" to QuantityValueFixture.oneUnit,
            "mass" to QuantityValueFixture.oneKilogram,
        )
        val innerConnector = mockk<DataSourceConnector<BasicNumber>>()
        every { innerConnector.getConfig() } returns ConnectorConfig(
            name = connectorName,
            options = emptyMap(),
        )
        every { innerConnector.getAll(any(), any(), any()) } returns sequenceOf(
            ERecord(mapOf(
                "geo" to EStringLiteral("FR"),
                "n_items" to QuantityFixture.oneUnit,
                "mass" to QuantityFixture.oneKilogram,
            )),
            ERecord(mapOf(
                "geo" to EStringLiteral("FR"),
                "n_items" to QuantityFixture.oneUnit,
                "mass" to QuantityFixture.twoKilograms,
            )),
        )
        val inMemoryConnector = InMemoryConnector<BasicNumber>(
            config = InMemoryConnectorKeys.defaultConfig(),
            content = mapOf(
                "no_match" to InMemoryDatasource(emptyList()),
            ),
        )
        val sourceOps = DefaultDataSourceOperations(
            BasicOperations,
            connectors = mapOf(connectorName to innerConnector, inMemoryConnector.getName() to inMemoryConnector),
        )
        val source = DataSourceValue(
            config = DataSourceConfig(
                name = "inventory",
                connector = connectorName
            ),
            schema = schema,
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
                "n_items" to QuantityFixture.oneUnit,
                "mass" to QuantityFixture.twoKilograms,
            )),
        )
        assertEquals(expected, actual)
    }


    @Test
    fun getFirst_override_whenMatchOverride() {
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
                    "geo" to str("UK"),
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
        val inMemoryConnector = InMemoryConnector(
            config = InMemoryConnectorKeys.defaultConfig(),
            content = mapOf(
                "inventory" to inMemoryDatasource,
            ),
        )
        val sourceOps = DefaultDataSourceOperations(
            BasicOperations,
            mapOf(inMemoryConnector.getName() to inMemoryConnector)
        )
        val source = DataSourceValue(
            config = DataSourceConfig(
                name = "inventory",
                connector = "in_memory",
            ),
            schema = schema,
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
    fun getFirst_override_whenNoMatchOverride() {
        // given
        val schema = mapOf(
            "geo" to StringValue("FR"),
            "n_items" to QuantityValueFixture.oneUnit,
            "mass" to QuantityValueFixture.oneKilogram,
        )
        val innerConnector = mockk<DataSourceConnector<BasicNumber>>()
        every { innerConnector.getConfig() } returns ConnectorConfig(
            name = connectorName,
            options = emptyMap(),
        )
        every { innerConnector.getFirst(any(), any(), any()) } returns ERecord(mapOf(
            "geo" to EStringLiteral("FR"),
            "n_items" to QuantityFixture.oneUnit,
            "mass" to QuantityFixture.oneKilogram,
        ))
        val inMemoryConnector = InMemoryConnector<BasicNumber>(
            config = InMemoryConnectorKeys.defaultConfig(),
            content = mapOf(
                "no_match" to InMemoryDatasource(emptyList()),
            ),
        )
        val sourceOps = DefaultDataSourceOperations(
            BasicOperations,
            connectors = mapOf(connectorName to innerConnector, inMemoryConnector.getName() to inMemoryConnector),
        )
        val source = DataSourceValue(
            config = DataSourceConfig(
                name = "inventory",
                connector = connectorName,
            ),
            schema = schema,
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
    fun sumProduct_override_whenMatchOverride() {
        // given
        val innerSourceOps = mockk<DataSourceOperations<BasicNumber>>()
        every { innerSourceOps.getAll(any()) } returns emptySequence()
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
                    "geo" to str("UK"),
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
        val inMemoryConnector = InMemoryConnector(
            config = InMemoryConnectorKeys.defaultConfig(),
            content = mapOf("inventory" to inMemoryDatasource),
        )
        val sourceOps = DefaultDataSourceOperations(
            ops = BasicOperations,
            connectors = mapOf(inMemoryConnector.getName() to inMemoryConnector),
        )
        val source = DataSourceValue(
            config = DataSourceConfig(
                name = "inventory",
                connector = "in_memory",
            ),
            schema = schema,
            filter = mapOf(
                "geo" to StringValue("FR")
            )
        )

        // when
        val actual = sourceOps.sumProduct(source, listOf("n_items", "mass"))

        // then
        val expected = EQuantityScale(
            BasicNumber(3.0),
            EUnitLiteral(
                UnitFixture.kg.symbol.multiply(UnitFixture.unit.symbol),
                1.0,
                UnitFixture.kg.dimension.multiply(UnitFixture.unit.dimension),
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun sumProduct_override_whenNoMatchOverride() {
        // given
        val innerConnector = mockk<DataSourceConnector<BasicNumber>>()
        every { innerConnector.getConfig() } returns ConnectorConfig(
            name = connectorName,
            options = emptyMap(),
        )
        every { innerConnector.getAll(any(), any(), any()) } returns sequenceOf(
            ERecord(mapOf(
                "geo" to EStringLiteral("FR"),
                "n_items" to QuantityFixture.oneUnit,
                "mass" to QuantityFixture.oneKilogram,
            )),
            ERecord(mapOf(
                "geo" to EStringLiteral("FR"),
                "n_items" to QuantityFixture.oneUnit,
                "mass" to QuantityFixture.twoKilograms,
            )),
        )
        val schema = mapOf(
            "geo" to StringValue("FR"),
            "n_items" to QuantityValueFixture.oneUnit,
            "mass" to QuantityValueFixture.oneKilogram,
        )
        val inMemoryConnector = InMemoryConnector<BasicNumber>(
            config = InMemoryConnectorKeys.defaultConfig(),
            content = mapOf(
                "no_match" to InMemoryDatasource(emptyList()),
            ),
        )
        val sourceOps = DefaultDataSourceOperations(
            ops = BasicOperations,
            connectors = mapOf(connectorName to innerConnector, inMemoryConnector.getName() to inMemoryConnector),
        )
        val source = DataSourceValue(
            config = DataSourceConfig(
                name = "inventory",
                connector = connectorName,
            ),
            schema = schema,
            filter = mapOf(
                "geo" to StringValue("FR")
            )
        )
        // when
        val actual = sourceOps.sumProduct(source, listOf("n_items", "mass"))

        // then
        val expected = EQuantityScale(
            BasicNumber(3.0),
            EUnitLiteral(
                UnitFixture.kg.symbol.multiply(UnitFixture.unit.symbol),
                1.0,
                UnitFixture.kg.dimension.multiply(UnitFixture.unit.dimension),
            ),
        )
        assertEquals(expected, actual)
    }

    @Test
    fun sumProduct_whenHugeSequence() {
        // given
        val connector = mockk<DataSourceConnector<BasicNumber>>()
        every { connector.getName() } returns connectorName
        every { connector.getConfig() } returns ConnectorConfig(
            name = connectorName,
            options = emptyMap(),
        )
        val n = 5000
        every { connector.getAll(any(), any(), any()) } returns (1..n)
            .map {
                ERecord(mapOf(
                    "geo" to EStringLiteral("FR"),
                    "n_items" to QuantityFixture.oneUnit,
                    "mass" to QuantityFixture.oneKilogram,
                ))
            }.asSequence()

        val builder = mockk<ConnectorBuilder<BasicNumber>>()
        every { builder.buildOrNull(any(), any()) } returns connector
        val factory = ConnectorFactory(".", config, ops, symbolTable, listOf(builder))
        val sourceOps = DefaultDataSourceOperations(ops, factory.buildConnectors(), emptyMap())
        val source = DataSourceValue(
            config = DataSourceConfig(
                name = sourceName,
                connector = connectorName,
            ),
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
            BasicNumber(n.toDouble()),
            UnitFixture.unit.times(UnitFixture.kg),
        )
        assertEquals(expected, actual)
    }
}
