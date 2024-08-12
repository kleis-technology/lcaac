package ch.kleis.lcaac.core.datasource.resilio_db.api

import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.lang.expression.DataExpression
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.expression.EStringLiteral
import ch.kleis.lcaac.core.lang.fixture.QuantityFixture
import ch.kleis.lcaac.core.lang.fixture.QuantityValueFixture
import ch.kleis.lcaac.core.lang.fixture.UnitFixture
import ch.kleis.lcaac.core.lang.register.DataKey
import ch.kleis.lcaac.core.lang.register.DataSourceRegister
import ch.kleis.lcaac.core.lang.value.DataValue
import ch.kleis.lcaac.core.lang.value.StringValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.core.prelude.Prelude
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test


class RdbRackServerDeserializerTest {
    private val reducer = DataExpressionReducer(
        dataRegister = Prelude.units<BasicNumber>().plus(
            mapOf(
                DataKey("GB") to UnitFixture.gb,
                DataKey("TB") to UnitFixture.gb,
            )
        ),
        dataSourceRegister = DataSourceRegister.empty(),
        ops = BasicOperations,
        sourceOps = mockk(),
    )


    @Test
    fun rdbRackServer_schema() {
        // given
        val deserializer = RdbRackServerDeserializer(
            primaryKey = "id",
            BasicOperations,
            this::eval
        )

        // when
        val actual = deserializer.schema()

        // then
        val expected: Map<String, DataValue<BasicNumber>> = mapOf(
            "id" to StringValue("server-01"),
            "model_name" to StringValue("model name"),
            "rack_unit" to QuantityValueFixture.oneUnit,
            "cpu_name" to StringValue("cpu name"),
            "cpu_quantity" to QuantityValueFixture.oneUnit,
            "ram_total_size_gb" to QuantityValueFixture.oneGb,
            "ssd_total_size_gb" to QuantityValueFixture.oneGb,
        )
        assertEquals(expected, actual)
    }

    private fun eval(it: DataExpression<BasicNumber>): DataValue<BasicNumber> {
        return with(ToValue(BasicOperations)) {
            reducer.reduce(it).toValue()
        }
    }

    @Test
    fun rdbRackServer_deserialize() {
        // given
        val sut = RdbRackServerDeserializer(
            primaryKey = "id",
            BasicOperations,
            this::eval
        )
        val record = ERecord(mapOf(
            "id" to EStringLiteral("server-01"),
            "model_name" to EStringLiteral("model name"),
            "rack_unit" to QuantityFixture.twoUnits,
            "cpu_name" to EStringLiteral("cpu name"),
            "cpu_quantity" to QuantityFixture.oneUnit,
            "ram_total_size_gb" to QuantityFixture.oneGb,
            "ssd_total_size_gb" to QuantityFixture.oneTb,
        ))

        // when
        val actual = sut.deserialize(record)

        // then
        val expected = RdbRackServer(
            id = "server-01",
            modelName = "model name",
            rackUnit = 2,
            cpuName = "cpu name",
            cpuQuantity = 1,
            ramTotalSizeGb = 1.0,
            ssdTotalSizeGb = 1000.0,
        )
        assertEquals(expected, actual)
    }
}
