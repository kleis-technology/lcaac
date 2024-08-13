package ch.kleis.lcaac.core.datasource.resilio_db

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.datasource.resilio_db.api.LcStepMapping
import ch.kleis.lcaac.core.datasource.resilio_db.api.SupportedEndpoint
import kotlin.test.Test
import kotlin.test.assertEquals

class RDbDataSourceOptionsTest {
    @Test
    fun defaultOptions() {
        // given
        val config = DataSourceConfig(
            name = "impacts",
            connector = ResilioDbConnectorKeys.RDB_CONNECTOR_NAME,
            options = mapOf(
                "paramsFrom" to "inventory",
                "endpoint" to "rack_server",
            )
        )

        // when
        val actual = RDbDataSourceOptions.from(config)

        // then
        val expected = RDbDataSourceOptions(
            primaryKey = "id",
            paramsFrom = "inventory",
            foreignKey = "id",
            endpoint = SupportedEndpoint.RACK_SERVER,
            lcStepMapping = LcStepMapping(
                key = "lc_step",
                manufacturing = "manufacturing",
                transport = "transport",
                use = "use",
                endOfLife = "end-of-life"
            )
        )
        assertEquals(expected, actual)
    }
}
