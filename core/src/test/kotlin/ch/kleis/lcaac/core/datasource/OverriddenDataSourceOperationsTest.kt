package ch.kleis.lcaac.core.datasource


import ch.kleis.lcaac.core.config.DataSourceConfig
import ch.kleis.lcaac.core.datasource.in_memory.InMemNum
import ch.kleis.lcaac.core.datasource.in_memory.InMemStr
import ch.kleis.lcaac.core.datasource.in_memory.InMemoryDatasource
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

class OverriddenDataSourceOperationsTest {

    @Test
    fun getAll_whenMatchOverride() {
        // given
        val innerSourceOps = mockk<DataSourceOperations<BasicNumber>>()
        every { innerSourceOps.getAll(any()) } returns emptySequence()
        val schema = mapOf(
            "geo" to StringValue("FR"),
            "n_items" to QuantityValueFixture.oneUnit,
            "mass" to QuantityValueFixture.oneKilogram,
        )
        val inMemoryDatasource = InMemoryDatasource(
            schema = schema,
            records = listOf(
                mapOf(
                    "geo" to InMemStr("FR"),
                    "n_items" to InMemNum(1.0),
                    "mass" to InMemNum(1.0),
                ),
                mapOf(
                    "geo" to InMemStr("UK"),
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
        val sourceOps = OverriddenDataSourceOperations(
            content = mapOf(
                "inventory" to inMemoryDatasource,
            ),
            BasicOperations,
            innerSourceOps,
        )
        val source = DataSourceValue(
            config = DataSourceConfig(
                name = "inventory",
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
    fun getAll_whenNoMatchOverride() {
        // given
        val innerSourceOps = mockk<DataSourceOperations<BasicNumber>>()
        val schema = mapOf(
            "geo" to StringValue("FR"),
            "n_items" to QuantityValueFixture.oneUnit,
            "mass" to QuantityValueFixture.oneKilogram,
        )
        val sourceOps = OverriddenDataSourceOperations(
            content = mapOf(
                "no_match" to InMemoryDatasource(schema, emptyList()),
            ),
            BasicOperations,
            innerSourceOps,
        )
        val source = DataSourceValue(
            config = DataSourceConfig(
                name = "inventory",
            ),
            schema = schema,
            filter = mapOf(
                "geo" to StringValue("FR")
            )
        )
        every { innerSourceOps.getAll(source) } returns sequenceOf(
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
    fun getFirst_whenMatchOverride() {
        // given
        val innerSourceOps = mockk<DataSourceOperations<BasicNumber>>()
        every { innerSourceOps.getAll(any()) } returns emptySequence()
        val schema = mapOf(
            "geo" to StringValue("FR"),
            "n_items" to QuantityValueFixture.oneUnit,
            "mass" to QuantityValueFixture.oneKilogram,
        )
        val inMemoryDatasource = InMemoryDatasource(
            schema = schema,
            records = listOf(
                mapOf(
                    "geo" to InMemStr("FR"),
                    "n_items" to InMemNum(1.0),
                    "mass" to InMemNum(1.0),
                ),
                mapOf(
                    "geo" to InMemStr("UK"),
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
        val sourceOps = OverriddenDataSourceOperations(
            content = mapOf(
                "inventory" to inMemoryDatasource,
            ),
            BasicOperations,
            innerSourceOps,
        )
        val source = DataSourceValue(
            config = DataSourceConfig(
                name = "inventory",
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
    fun getFirst_whenNoMatchOverride() {
        // given
        val innerSourceOps = mockk<DataSourceOperations<BasicNumber>>()
        val schema = mapOf(
            "geo" to StringValue("FR"),
            "n_items" to QuantityValueFixture.oneUnit,
            "mass" to QuantityValueFixture.oneKilogram,
        )
        val sourceOps = OverriddenDataSourceOperations(
            content = mapOf(
                "no_match" to InMemoryDatasource(schema, emptyList()),
            ),
            BasicOperations,
            innerSourceOps,
        )
        val source = DataSourceValue(
            config = DataSourceConfig(
                name = "inventory",
            ),
            schema = schema,
            filter = mapOf(
                "geo" to StringValue("FR")
            )
        )
        every { innerSourceOps.getFirst(source) } returns ERecord(mapOf(
            "geo" to EStringLiteral("FR"),
            "n_items" to QuantityFixture.oneUnit,
            "mass" to QuantityFixture.oneKilogram,
        ))

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
    fun sumProduct_whenMatchOverride() {
        // given
        val innerSourceOps = mockk<DataSourceOperations<BasicNumber>>()
        every { innerSourceOps.getAll(any()) } returns emptySequence()
        val schema = mapOf(
            "geo" to StringValue("FR"),
            "n_items" to QuantityValueFixture.oneUnit,
            "mass" to QuantityValueFixture.oneKilogram,
        )
        val inMemoryDatasource = InMemoryDatasource(
            schema = schema,
            records = listOf(
                mapOf(
                    "geo" to InMemStr("FR"),
                    "n_items" to InMemNum(1.0),
                    "mass" to InMemNum(1.0),
                ),
                mapOf(
                    "geo" to InMemStr("UK"),
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
        val sourceOps = OverriddenDataSourceOperations(
            content = mapOf(
                "inventory" to inMemoryDatasource,
            ),
            BasicOperations,
            innerSourceOps,
        )
        val source = DataSourceValue(
            config = DataSourceConfig(
                name = "inventory",
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
    fun sumProduct_whenNoMatchOverride() {
        // given
        val innerSourceOps = mockk<DataSourceOperations<BasicNumber>>()
        val schema = mapOf(
            "geo" to StringValue("FR"),
            "n_items" to QuantityValueFixture.oneUnit,
            "mass" to QuantityValueFixture.oneKilogram,
        )
        val sourceOps = OverriddenDataSourceOperations(
            content = mapOf(
                "no_match" to InMemoryDatasource(schema, emptyList()),
            ),
            BasicOperations,
            innerSourceOps,
        )
        val source = DataSourceValue(
            config = DataSourceConfig(
                name = "inventory",
            ),
            schema = schema,
            filter = mapOf(
                "geo" to StringValue("FR")
            )
        )
        every { innerSourceOps.sumProduct(source, listOf("n_items", "mass")) } returns
            EQuantityScale(
                BasicNumber(7.0),
                EUnitLiteral(
                    UnitFixture.kg.symbol.multiply(UnitFixture.unit.symbol),
                    1.0,
                    UnitFixture.kg.dimension.multiply(UnitFixture.unit.dimension),
                ),
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
