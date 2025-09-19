package ch.kleis.lcaac.core.lang

import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.lang.expression.EDataSource
import ch.kleis.lcaac.core.lang.expression.EStringLiteral
import ch.kleis.lcaac.core.lang.register.DataSourceKey
import ch.kleis.lcaac.core.lang.register.DataSourceRegister
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertEquals

class SymbolTableTest {
    @Nested
    inner class OverrideDatasourceConnector {
        @Test
        fun `when key does not exist, throws exception`() {
            // Given
            val dataSourceKey = DataSourceKey("some-key")
            val symbolTable = SymbolTable<BasicNumber>()

            // When + Then
            val exception = assertThrows(IllegalStateException::class.java) {
                symbolTable.overrideDatasourceConnector(dataSourceKey, "some_connector")
            }
            assertEquals("DataSource some-key does not exist", exception.message)
        }

        @Test
        fun `when key exists, override connector`(){
            // Given
            val dataSourceKey = DataSourceKey("some-key")
            val dataSource = EDataSource<BasicNumber>(
                config = DataSourceConfig("some-key", "csv", "loc.csv", "id", mapOf("key1" to "value1")),
                schema = mapOf("key2" to EStringLiteral("value2")),
                filter = mapOf("key3" to EStringLiteral("value3"))
            )

            val otherDataSourceKey = DataSourceKey("some-other-key")
            val otherDataSource = EDataSource<BasicNumber>(
                config = DataSourceConfig("some-other-key"),
                schema = mapOf()
            )
            val dataSourceRegister = DataSourceRegister(
                data = mapOf(dataSourceKey to dataSource, otherDataSourceKey to otherDataSource)
            )
            val symbolTable = SymbolTable(dataSources = dataSourceRegister)

            // When
            val actual = symbolTable.overrideDatasourceConnector(dataSourceKey, "in_memory")

            // Then
            assertEquals("in_memory", actual.dataSources[dataSourceKey]?.config?.connector)

            // left unchanged
            val actualConfig = actual.dataSources[dataSourceKey]?.config
            assertEquals(dataSource.config.name, actualConfig?.name)
            assertEquals(dataSource.config.location, actualConfig?.location)
            assertEquals(dataSource.config.primaryKey, actualConfig?.primaryKey)
            assertEquals(dataSource.config.options, actualConfig?.options)

            val actualDataSource = actual.dataSources[dataSourceKey]
            assertEquals(dataSource.schema, actualDataSource?.schema)
            assertEquals(dataSource.filter, actualDataSource?.filter)

            assertEquals(otherDataSource, actual.dataSources[otherDataSourceKey])
        }
    }
}