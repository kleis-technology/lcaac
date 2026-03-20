package ch.kleis.lcaac.cli.csv.trace

import ch.kleis.lcaac.cli.cmd.OutputFormat
import ch.kleis.lcaac.cli.csv.CsvRequest
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


class TraceCsvResultWriterTest {
    private val kWh = UnitValue<BasicNumber>(UnitSymbol.of("kWh"), 1000.0, Dimension.of("energy"))
    private val kg = UnitValue<BasicNumber>(UnitSymbol.of("kg"), 1.0, Dimension.of("mass"))
    private val u = UnitValue<BasicNumber>(UnitSymbol.of("u"), 1.0, Dimension.None)

    private val request = CsvRequest(
        "main",
        emptyMap(),
        header = mapOf("id" to 0, "x" to 1),
        record = listOf("id-0", "0.5")
    )
    private val demandedProduct = TechnoExchangeValue(
        QuantityValue(BasicNumber(1.0), u),
        ProductValue("foo", u, FromProcessRefValue("foo")),
    )
    private val output = ProductValue(
        name = "electricity",
        referenceUnit = kWh,
        fromProcessRef = FromProcessRefValue("main", arguments = mapOf("x" to QuantityValue(BasicNumber(0.5), u)))
    )
    private val supply = QuantityValue(BasicNumber(1.0), kWh)
    private val impacts = mapOf(
        IndicatorValue("co2", kg) as MatrixColumnIndex<BasicNumber> to QuantityValue(BasicNumber(0.5), kg)
    )

    private fun result(vararg depths: Int) = TraceCsvResult(
        request,
        trace = depths.map { depth ->
            TraceCsvResultItem(depth, demandedProduct, supply, output, impacts)
        }
    )

    @Test
    fun `csv header`() {
        val writer = TraceCsvResultWriter(OutputFormat.CSV)
        assertEquals(
            "id,x,depth,d_amount,d_unit,d_product,alloc,name,a,b,c,amount,unit,co2,co2_unit\n",
            writer.header(result(0))
        )
    }

    @Test
    fun `csv rows`() {
        val writer = TraceCsvResultWriter(OutputFormat.CSV)
        assertEquals(
            listOf(
                "id-0,0.5,0,1.0,u,foo,1.0,electricity,main,{},{x=0.5 u},1.0,kWh,0.5,kg\n",
                "id-0,0.5,1,1.0,u,foo,1.0,electricity,main,{},{x=0.5 u},1.0,kWh,0.5,kg\n",
            ),
            writer.rows(result(0, 1))
        )
    }

    @Test
    fun `csv footer`() {
        val writer = TraceCsvResultWriter(OutputFormat.CSV)
        assertEquals("", writer.footer())
    }

    @Test
    fun `text format`() {
        val writer = TraceCsvResultWriter(OutputFormat.TEXT)
        val result = result(0, 1)

        assertEquals("", writer.header(result))
        assertEquals(emptyList<String>(), writer.rows(result))

        // column widths driven by "electricity" (name col) and "{x=0.5 u}" (c col)
        val footer = writer.footer()
        val lines = footer.lines().filter { it.isNotEmpty() }
        assertEquals(4, lines.size)  // header, separator, row0, row1

        // header line
        assertEquals("id    x    depth  d_amount  d_unit  d_product  alloc  name         a     b   c          amount  unit  co2  co2_unit", lines[0])
        // separator line (no trimEnd applied)
        assertEquals("----  ---  -----  --------  ------  ---------  -----  -----------  ----  --  ---------  ------  ----  ---  --------", lines[1])
        // data rows
        assertEquals("id-0  0.5  0      1.0       u       foo        1.0    electricity  main  {}  {x=0.5 u}  1.0     kWh   0.5  kg", lines[2])
        assertEquals("id-0  0.5  1      1.0       u       foo        1.0    electricity  main  {}  {x=0.5 u}  1.0     kWh   0.5  kg", lines[3])
    }

    @Test
    fun `json format`() {
        val writer = TraceCsvResultWriter(OutputFormat.JSON)
        val result = result(0)

        assertEquals("[\n", writer.header(result))
        val rows = writer.rows(result)
        assertEquals(1, rows.size)
        val json = rows[0]
        assertEquals(true, json.startsWith("{"))
        assertEquals(true, json.contains("\"depth\": 0"))
        assertEquals(true, json.contains("\"name\": \"electricity\""))
        assertEquals(true, json.contains("\"co2\": {\"amount\": 0.5, \"unit\": \"kg\"}"))
        assertEquals("\n]\n", writer.footer())
    }

    @Test
    fun `json format, two results separated by comma`() {
        val writer = TraceCsvResultWriter(OutputFormat.JSON)
        writer.header(result(0))
        val first = writer.rows(result(0))[0]
        val second = writer.rows(result(1))[0]

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
        val resultWithTwo = TraceCsvResult(
            request,
            trace = listOf(TraceCsvResultItem(0, demandedProduct, supply, output, twoImpacts))
        )
        val writer = TraceCsvResultWriter(OutputFormat.CSV, indicators = setOf("co2"))

        assertEquals(
            "id,x,depth,d_amount,d_unit,d_product,alloc,name,a,b,c,amount,unit,co2,co2_unit\n",
            writer.header(resultWithTwo)
        )
        assertEquals(
            listOf("id-0,0.5,0,1.0,u,foo,1.0,electricity,main,{},{x=0.5 u},1.0,kWh,0.5,kg\n"),
            writer.rows(resultWithTwo)
        )
    }
}
