package ch.kleis.lcaac.cli.csv.trace

import ch.kleis.lcaac.cli.csv.CsvRequest
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import kotlin.test.Test
import kotlin.test.assertEquals


class TraceCsvResultWriterTest {
    @Test
    fun header() {
        // given
        val request = CsvRequest(
            "main",
            emptyMap(),
            header = mapOf("id" to 0, "x" to 1),
            record = listOf("id-0", "0.5")
        )
        val kWh = UnitValue<BasicNumber>(UnitSymbol.of("kWh"), 1000.0, Dimension.of("energy"))
        val kg = UnitValue<BasicNumber>(UnitSymbol.of("kg"), 1.0, Dimension.of("mass"))
        val u = UnitValue<BasicNumber>(UnitSymbol.of("u"), 1.0, Dimension.None)
        val output = ProductValue(
            name = "electricity",
            referenceUnit = kWh,
            fromProcessRef = FromProcessRefValue(
                "main",
                arguments = mapOf(
                    "x" to QuantityValue(BasicNumber(0.5), u)
                )
            )
        )
        val impacts = mapOf(
            IndicatorValue("co2", kg) as MatrixColumnIndex<BasicNumber>
                    to QuantityValue(BasicNumber(0.5), kg)
        )
        val result = TraceCsvResult(
            request,
            trace = listOf(
                TraceCsvResultItem(
                    1,
                    output,
                    impacts,
                ),
                TraceCsvResultItem(
                    2,
                    output,
                    impacts,
                )
            )
        )
        val writer = TraceCsvResultWriter()

        // when
        val actual = writer.header(result)

        // then
        assertEquals("id,x,depth,product,amount,reference unit,co2,co2_unit\n", actual)
    }

    @Test
    fun rows() {
        // given
        val request = CsvRequest(
            "main",
            emptyMap(),
            header = mapOf("id" to 0, "x" to 1),
            record = listOf("id-0", "0.5")
        )
        val kWh = UnitValue<BasicNumber>(UnitSymbol.of("kWh"), 1000.0, Dimension.of("energy"))
        val kg = UnitValue<BasicNumber>(UnitSymbol.of("kg"), 1.0, Dimension.of("mass"))
        val u = UnitValue<BasicNumber>(UnitSymbol.of("u"), 1.0, Dimension.None)
        val output = ProductValue(
            name = "electricity",
            referenceUnit = kWh,
            fromProcessRef = FromProcessRefValue(
                "main",
                arguments = mapOf(
                    "x" to QuantityValue(BasicNumber(0.5), u)
                )
            )
        )
        val impacts = mapOf(
            IndicatorValue("co2", kg) as MatrixColumnIndex<BasicNumber>
                    to QuantityValue(BasicNumber(0.5), kg)
        )
        val result = TraceCsvResult(
            request,
            trace = listOf(
                TraceCsvResultItem(
                    1,
                    output,
                    impacts,
                ),
                TraceCsvResultItem(
                    2,
                    output,
                    impacts,
                )
            )
        )
        val writer = TraceCsvResultWriter()

        // when
        val actual = writer.rows(result)

        // then
        assertEquals(
            listOf(
                "id-0,0.5,1,electricity,1.0,kWh,0.5,kg\n",
                "id-0,0.5,2,electricity,1.0,kWh,0.5,kg\n",
            ), actual
        )
    }
}
