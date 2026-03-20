package ch.kleis.lcaac.cli.csv.assess

import ch.kleis.lcaac.cli.cmd.OutputFormat
import ch.kleis.lcaac.cli.csv.CsvRequest
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import kotlin.test.Test
import kotlin.test.assertEquals


class AssessCsvResultWriterTest {
    private val kWh = UnitValue<BasicNumber>(UnitSymbol.of("kWh"), 1000.0, Dimension.of("energy"))
    private val kg = UnitValue<BasicNumber>(UnitSymbol.of("kg"), 1.0, Dimension.of("mass"))
    private val u = UnitValue<BasicNumber>(UnitSymbol.of("u"), 1.0, Dimension.None)

    private fun result(impacts: Map<MatrixColumnIndex<BasicNumber>, QuantityValue<BasicNumber>>): AssessCsvResult {
        val request = CsvRequest(
            "main",
            emptyMap(),
            header = mapOf("id" to 0, "x" to 1),
            record = listOf("id-0", "0.5")
        )
        val output = ProductValue(
            name = "electricity",
            referenceUnit = kWh,
            fromProcessRef = FromProcessRefValue("main", arguments = mapOf("x" to QuantityValue(BasicNumber(0.5), u)))
        )
        return AssessCsvResult(request, output, impacts)
    }

    private val impacts = mapOf(
        IndicatorValue("co2", kg) as MatrixColumnIndex<BasicNumber> to QuantityValue(BasicNumber(0.5), kg)
    )

    @Test
    fun `csv header`() {
        val writer = AssessCsvResultWriter(OutputFormat.CSV)
        assertEquals("id,x,product,amount,reference unit,co2,co2_unit\n", writer.header(result(impacts)))
    }

    @Test
    fun `csv row`() {
        val writer = AssessCsvResultWriter(OutputFormat.CSV)
        assertEquals("id-0,0.5,electricity,1.0,kWh,0.5,kg\n", writer.row(result(impacts)))
    }

    @Test
    fun `csv footer`() {
        val writer = AssessCsvResultWriter(OutputFormat.CSV)
        assertEquals("", writer.footer())
    }

    @Test
    fun `text format`() {
        val writer = AssessCsvResultWriter(OutputFormat.TEXT)
        val result = result(impacts)

        assertEquals("", writer.header(result))
        assertEquals("", writer.row(result))
        assertEquals(
            "id    x    product      amount  reference unit  co2  co2_unit\n" +
                "----  ---  -----------  ------  --------------  ---  --------\n" +
                "id-0  0.5  electricity  1.0     kWh             0.5  kg\n",
            writer.footer()
        )
    }

    @Test
    fun `json format`() {
        val writer = AssessCsvResultWriter(OutputFormat.JSON)
        val result = result(impacts)

        assertEquals("[\n", writer.header(result))
        assertEquals(
            "{\n" +
                "  \"request\": {\"id\": \"id-0\", \"x\": \"0.5\"},\n" +
                "  \"product\": \"electricity\",\n" +
                "  \"amount\": 1.0,\n" +
                "  \"reference_unit\": \"kWh\",\n" +
                "  \"impacts\": {\"co2\": {\"amount\": 0.5, \"unit\": \"kg\"}}\n" +
                "}",
            writer.row(result)
        )
        assertEquals("\n]\n", writer.footer())
    }

    @Test
    fun `json format, two rows separated by comma`() {
        val writer = AssessCsvResultWriter(OutputFormat.JSON)
        val result = result(impacts)
        writer.header(result)
        val first = writer.row(result)
        val second = writer.row(result)

        assertEquals(true, first.startsWith("{"))
        assertEquals(true, second.startsWith(",\n{"))
    }

    @Test
    fun `indicators filter`() {
        val ch4 = IndicatorValue("ch4", kg) as MatrixColumnIndex<BasicNumber>
        val co2 = IndicatorValue("co2", kg) as MatrixColumnIndex<BasicNumber>
        val twoImpacts = mapOf(
            co2 to QuantityValue(BasicNumber(0.5), kg),
            ch4 to QuantityValue(BasicNumber(1.2), kg),
        )
        val writer = AssessCsvResultWriter(OutputFormat.CSV, indicators = setOf("co2"))

        assertEquals("id,x,product,amount,reference unit,co2,co2_unit\n", writer.header(result(twoImpacts)))
        assertEquals("id-0,0.5,electricity,1.0,kWh,0.5,kg\n", writer.row(result(twoImpacts)))
    }
}
