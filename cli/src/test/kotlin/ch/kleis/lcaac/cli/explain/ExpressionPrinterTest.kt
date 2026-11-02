package ch.kleis.lcaac.cli.explain

import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import kotlin.test.Test
import kotlin.test.assertEquals

class ExpressionPrinterTest {
    private val fmt = NumberFormat(6)
    private val printer = ExpressionPrinter(fmt)

    private fun ref(name: String) = EDataRef<BasicNumber>(name)
    private fun scale(x: Double, base: DataExpression<BasicNumber>) = EQuantityScale(BasicNumber(x), base)

    @Test
    fun `quantities and arithmetic`() {
        val expression = EQuantityMul(
            EQuantityDiv(ref("a"), scale(80.0, ref("percent"))),
            EQuantityAdd(scale(1.0, ref("u")), ref("b")),
        )
        assertEquals("a / (80 percent) * (1 u + b)", printer.print(expression))
    }

    @Test
    fun `subtraction keeps right operand grouped`() {
        val expression = EQuantitySub(ref("a"), EQuantitySub(ref("b"), ref("c")))
        assertEquals("a - (b - c)", printer.print(expression))
    }

    @Test
    fun `scale of a product is printed as written`() {
        val expression = scale(10.0, EQuantityMul(ref("percent"), ERecordEntry(ref("row"), "mass")))
        assertEquals("10 percent * row.mass", printer.print(expression))
    }

    @Test
    fun `scale of a sum keeps parentheses`() {
        val expression = scale(2.0, EQuantityAdd(ref("a"), ref("b")))
        assertEquals("2 (a + b)", printer.print(expression))
    }

    @Test
    fun `power`() {
        assertEquals("(a * b)^2", printer.print(EQuantityPow(EQuantityMul(ref("a"), ref("b")), 2.0)))
    }

    @Test
    fun `data source primitives`() {
        val source = EFilter<BasicNumber>(EDataSourceRef("machines"), mapOf("id" to ref("machine")))
        assertEquals("lookup machines match id = machine", printer.print(EFirstRecordOf(source)))
        assertEquals("default_record from machines", printer.print(EDefaultRecordOf(EDataSourceRef("machines"))))
        val twoFilters = EFilter(source, mapOf("geo" to EStringLiteral("FR")))
        assertEquals(
            "sum(machines match (geo = \"FR\", id = machine), power * hours)",
            printer.print(ESumProduct(twoFilters, listOf("power", "hours"))),
        )
    }

    @Test
    fun `substitution replaces references`() {
        val substituting = ExpressionPrinter(fmt) { e -> if (e is EDataRef && e.name == "a") "2 kg" else null }
        assertEquals("2 kg * b", substituting.print(EQuantityMul(ref("a"), ref("b"))))
        assertEquals("b / (2 kg)", substituting.print(EQuantityDiv(ref("b"), ref("a"))))
    }

    @Test
    fun `references include data source filters`() {
        val source = EFilter<BasicNumber>(EDataSourceRef("machines"), mapOf("id" to ref("machine")))
        val expression = EQuantityMul(ERecordEntry(EFirstRecordOf(source), "power"), scale(2.0, ref("h")))
        assertEquals(setOf("machine", "h"), referencesOf(expression))
    }

    @Test
    fun `number format`() {
        assertEquals("0", fmt.format(0.0))
        assertEquals("0.1", fmt.format(0.1))
        assertEquals("0.3", fmt.format(0.1 + 0.2))
        assertEquals("162960", fmt.format(162960.0))
        assertEquals("1.23457e-7", fmt.format(1.234567e-7))
        assertEquals("1000000000000", fmt.format(1e12))
        assertEquals("1e15", fmt.format(1e15))
        assertEquals("-2.5", fmt.format(-2.5))
    }

    @Test
    fun `whole numbers are exact`() {
        assertEquals("34359738368", fmt.format(34359738368.0))
        assertEquals("1073741824", fmt.format(1073741824.0))
        assertEquals("1116743", fmt.format(1116743.0))
        assertEquals("1116743.5", NumberFormat(8).format(1116743.5))
        assertEquals("1116740", fmt.format(1116743.5))
    }
}
