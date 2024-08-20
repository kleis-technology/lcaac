package ch.kleis.lcaac.core.datasource.resilio_db

import ch.kleis.lcaac.core.config.ConnectorConfig
import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.DataSourceConnector
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.datasource.resilio_db.api.RdbClient
import ch.kleis.lcaac.core.datasource.resilio_db.api.requests.RdbRackServer
import ch.kleis.lcaac.core.datasource.resilio_db.api.requests.RdbSwitch
import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.expression.EStringLiteral
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture
import ch.kleis.lcaac.core.lang.fixture.UnitFixture
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.lang.value.DataSourceValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.core.prelude.Prelude
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ResilioDbConnectorTest {
    private class Context(
        inventory: List<ERecord<BasicNumber>> = emptyList(),
        onRequestRackServer: List<ERecord<BasicNumber>> = emptyList(),
        onRequestSwitch: List<ERecord<BasicNumber>> = emptyList(),
    ) {
        val caller = mockk<DefaultDataSourceOperations<BasicNumber>>()
        val rdbConnector: DataSourceConnector<BasicNumber>
        val rdbClient = mockk<RdbClient<BasicNumber>>()

        init {
            val inventoryConfig = DataSourceConfig(
                name = "inventory",
                connector = "inventory"
            )
            val inventoryConnectorConfig = ConnectorConfig(
                name = "inventory",
                options = emptyMap(),
            )
            every { caller.getAll(any()) } returns inventory.asSequence()

            val lcaacConfig = LcaacConfig(
                datasources = listOf(inventoryConfig),
                connectors = listOf(inventoryConnectorConfig)
            )
            every { caller.getConfig() } returns lcaacConfig
            val symbolTable = SymbolTable(
                data = Prelude.units<BasicNumber>().plus(
                    mapOf(
                        DataKey("GB") to UnitFixture.gb,
                    )
                )
            )
            val factory = mockk<ConnectorFactory<BasicNumber>>()
            every { factory.getSymbolTable() } returns symbolTable
            every { factory.getQuantityOperations() } returns BasicOperations
            every { factory.getLcaacConfig() } returns lcaacConfig

            val rdbUrl = "https://test.db.resilio.tech"
            val rdbAccessToken = "my-access-token"
            val connectorConfig = ConnectorConfig(
                name = ResilioDbConnectorKeys.RDB_CONNECTOR_NAME,
                options = mapOf(
                    ResilioDbConnectorKeys.RDB_URL to rdbUrl,
                    ResilioDbConnectorKeys.RDB_ACCESS_TOKEN to rdbAccessToken,
                )
            )
            every { rdbClient.serverRack(any()) } returns onRequestRackServer
            every { rdbClient.switch(any()) } returns onRequestSwitch
            rdbConnector = ResilioDbConnector(
                config = connectorConfig,
                symbolTable = symbolTable,
                ops = BasicOperations,
                url = rdbUrl,
                accessToken = rdbAccessToken,
                rdbClientSupplier = { _, _ -> rdbClient }
            )
        }
    }

    @Test
    fun getAll_rackServer() {
        // given
        val onRequestRackServer = listOf(
            ERecord(mapOf(
                "id" to EStringLiteral("foo"),
                "lc_step" to EStringLiteral("manufacturing"),
                "GWP" to QuantityFixture.oneKilogram,
            )),
            ERecord(mapOf(
                "id" to EStringLiteral("foo"),
                "lc_step" to EStringLiteral("transport"),
                "GWP" to QuantityFixture.twoKilograms,
            )),
            ERecord(mapOf(
                "id" to EStringLiteral("foo"),
                "lc_step" to EStringLiteral("use"),
                "GWP" to QuantityFixture.zeroKilogram,
            )),
            ERecord(mapOf(
                "id" to EStringLiteral("foo"),
                "lc_step" to EStringLiteral("end-of-life"),
                "GWP" to QuantityFixture.oneKilogram,
            )),
        )
        val context = Context(
            inventory = listOf(
                ERecord(mapOf(
                    "id" to EStringLiteral("server-01"),
                    "model_name" to EStringLiteral("model name"),
                    "rack_unit" to QuantityFixture.oneUnit,
                    "cpu_name" to EStringLiteral("cpu name"),
                    "cpu_quantity" to QuantityFixture.twoUnits,
                    "ram_total_size_gb" to QuantityFixture.oneGb,
                    "ssd_total_size_gb" to QuantityFixture.oneTb,
                ))
            ),
            onRequestRackServer = onRequestRackServer
        )
        val config = DataSourceConfig(
            name = "impacts",
            connector = ResilioDbConnectorKeys.RDB_CONNECTOR_NAME,
            options = mapOf(
                "paramsFrom" to "inventory",
                "endpoint" to "rack_server",
            )
        )
        val source = DataSourceValue<BasicNumber>(
            config = config,
            schema = emptyMap(),
            filter = emptyMap(),
        )

        // when
        val actual = context.rdbConnector.getAll(context.caller, config, source).toList()

        // then
        val expected = onRequestRackServer
        assertEquals(expected, actual)
        verify {
            context.rdbClient.serverRack(RdbRackServer(
                id = "server-01",
                modelName = "model name",
                cpuName = "cpu name",
                rackUnit = 1,
                cpuQuantity = 2,
                ramTotalSizeGb = 1.0,
                ssdTotalSizeGb = 1000.0,
            ))
        }
    }

    @Test
    fun getAll_rackServer_filterByLcStep() {
        // given
        val onRequestRackServer = listOf(
            ERecord(mapOf(
                "id" to EStringLiteral("foo"),
                "lc_step" to EStringLiteral("manufacturing"),
                "GWP" to QuantityFixture.oneKilogram,
            )),
            ERecord(mapOf(
                "id" to EStringLiteral("foo"),
                "lc_step" to EStringLiteral("transport"),
                "GWP" to QuantityFixture.twoKilograms,
            )),
            ERecord(mapOf(
                "id" to EStringLiteral("foo"),
                "lc_step" to EStringLiteral("use"),
                "GWP" to QuantityFixture.zeroKilogram,
            )),
            ERecord(mapOf(
                "id" to EStringLiteral("foo"),
                "lc_step" to EStringLiteral("end-of-life"),
                "GWP" to QuantityFixture.oneKilogram,
            )),
        )
        val context = Context(
            inventory = listOf(
                ERecord(mapOf(
                    "id" to EStringLiteral("server-01"),
                    "model_name" to EStringLiteral("model name"),
                    "rack_unit" to QuantityFixture.oneUnit,
                    "cpu_name" to EStringLiteral("cpu name"),
                    "cpu_quantity" to QuantityFixture.twoUnits,
                    "ram_total_size_gb" to QuantityFixture.oneGb,
                    "ssd_total_size_gb" to QuantityFixture.oneTb,
                ))
            ),
            onRequestRackServer = onRequestRackServer
        )
        val config = DataSourceConfig(
            name = "impacts",
            connector = ResilioDbConnectorKeys.RDB_CONNECTOR_NAME,
            options = mapOf(
                "paramsFrom" to "inventory",
                "endpoint" to "rack_server",
            )
        )
        val source = DataSourceValue<BasicNumber>(
            config = config,
            schema = emptyMap(),
            filter = mapOf(
                "lc_step" to StringValue("manufacturing")
            ),
        )

        // when
        val actual = context.rdbConnector.getAll(context.caller, config, source).toList()

        // then
        val expected = listOf(ERecord(mapOf(
            "id" to EStringLiteral("foo"),
            "lc_step" to EStringLiteral("manufacturing"),
            "GWP" to QuantityFixture.oneKilogram,
        )))
        assertEquals(expected, actual)
        verify {
            context.rdbClient.serverRack(RdbRackServer(
                id = "server-01",
                modelName = "model name",
                cpuName = "cpu name",
                rackUnit = 1,
                cpuQuantity = 2,
                ramTotalSizeGb = 1.0,
                ssdTotalSizeGb = 1000.0,
            ))
        }
    }

    @Test
    fun getAll_switch() {
        // given
        val onRequestSwitch = listOf(
            ERecord(mapOf(
                "id" to EStringLiteral("foo"),
                "lc_step" to EStringLiteral("manufacturing"),
                "GWP" to QuantityFixture.oneKilogram,
            )),
            ERecord(mapOf(
                "id" to EStringLiteral("foo"),
                "lc_step" to EStringLiteral("transport"),
                "GWP" to QuantityFixture.twoKilograms,
            )),
            ERecord(mapOf(
                "id" to EStringLiteral("foo"),
                "lc_step" to EStringLiteral("use"),
                "GWP" to QuantityFixture.zeroKilogram,
            )),
            ERecord(mapOf(
                "id" to EStringLiteral("foo"),
                "lc_step" to EStringLiteral("end-of-life"),
                "GWP" to QuantityFixture.oneKilogram,
            )),
        )
        val context = Context(
            inventory = listOf(
                ERecord(mapOf(
                    "id" to EStringLiteral("server-01"),
                    "model_name" to EStringLiteral("model name"),
                    "rack_unit" to QuantityFixture.oneUnit,
                    "cpu_name" to EStringLiteral("cpu name"),
                    "cpu_quantity" to QuantityFixture.twoUnits,
                    "ram_total_size_gb" to QuantityFixture.oneGb,
                    "ssd_total_size_gb" to QuantityFixture.oneTb,
                ))
            ),
            onRequestSwitch = onRequestSwitch
        )
        val config = DataSourceConfig(
            name = "impacts",
            connector = ResilioDbConnectorKeys.RDB_CONNECTOR_NAME,
            options = mapOf(
                "paramsFrom" to "inventory",
                "endpoint" to "switch",
            )
        )
        val source = DataSourceValue<BasicNumber>(
            config = config,
            schema = emptyMap(),
            filter = emptyMap(),
        )

        // when
        val actual = context.rdbConnector.getAll(context.caller, config, source).toList()

        // then
        val expected = onRequestSwitch
        assertEquals(expected, actual)
        verify {
            context.rdbClient.switch(RdbSwitch(
                id = "server-01",
                cpuName = "cpu name",
                cpuQuantity = 2,
                ramTotalSizeGb = 1.0,
            ))
        }
    }

    @Test
    fun getAll_switch_filterByLcStep() {
        // given
        val onRequestSwitch = listOf(
            ERecord(mapOf(
                "id" to EStringLiteral("foo"),
                "lc_step" to EStringLiteral("manufacturing"),
                "GWP" to QuantityFixture.oneKilogram,
            )),
            ERecord(mapOf(
                "id" to EStringLiteral("foo"),
                "lc_step" to EStringLiteral("transport"),
                "GWP" to QuantityFixture.twoKilograms,
            )),
            ERecord(mapOf(
                "id" to EStringLiteral("foo"),
                "lc_step" to EStringLiteral("use"),
                "GWP" to QuantityFixture.zeroKilogram,
            )),
            ERecord(mapOf(
                "id" to EStringLiteral("foo"),
                "lc_step" to EStringLiteral("end-of-life"),
                "GWP" to QuantityFixture.oneKilogram,
            )),
        )
        val context = Context(
            inventory = listOf(
                ERecord(mapOf(
                    "id" to EStringLiteral("server-01"),
                    "model_name" to EStringLiteral("model name"),
                    "rack_unit" to QuantityFixture.oneUnit,
                    "cpu_name" to EStringLiteral("cpu name"),
                    "cpu_quantity" to QuantityFixture.twoUnits,
                    "ram_total_size_gb" to QuantityFixture.oneGb,
                    "ssd_total_size_gb" to QuantityFixture.oneTb,
                ))
            ),
            onRequestSwitch = onRequestSwitch
        )
        val config = DataSourceConfig(
            name = "impacts",
            connector = ResilioDbConnectorKeys.RDB_CONNECTOR_NAME,
            options = mapOf(
                "paramsFrom" to "inventory",
                "endpoint" to "switch",
            )
        )
        val source = DataSourceValue<BasicNumber>(
            config = config,
            schema = emptyMap(),
            filter = mapOf(
                "lc_step" to StringValue("use")
            ),
        )

        // when
        val actual = context.rdbConnector.getAll(context.caller, config, source).toList()

        // then
        val expected = listOf(
            ERecord(mapOf(
                "id" to EStringLiteral("foo"),
                "lc_step" to EStringLiteral("use"),
                "GWP" to QuantityFixture.zeroKilogram,
            ))
        )
        assertEquals(expected, actual)
        verify {
            context.rdbClient.switch(RdbSwitch(
                id = "server-01",
                cpuName = "cpu name",
                cpuQuantity = 2,
                ramTotalSizeGb = 1.0,
            ))
        }
    }
}
