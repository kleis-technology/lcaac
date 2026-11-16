package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import kotlin.test.Test
import kotlin.test.assertEquals

class DataExpressionPrinterTest {
    private val mapper = CoreMapper(BasicOperations)
    private val printer = DataExpressionPrinter(BasicOperations)

    private fun parse(text: String): DataExpression<BasicNumber> = mapper.dataExpression(parseDataExpression(text))

    private val written = listOf(
        "1 kg",
        "-2.5 kg",
        "1.5E-9 kg",
        "a + b - c",
        "a - (b - c)",
        "a - (b + c)",
        "a * b / c",
        "a / (b * c)",
        "a * (b + c)",
        "(a * b)^2",
        "a^2^3",
        "2 (a + b)",
        "2 a * b",
        "10 percent * part.mass",
        "a * 2 b",
        "a / (2 b)",
        "mach.consumption * mach.runs / efficiency * (1 u + grid_loss)",
        "\"FR\"",
        "lookup machines match id = machine",
        "lookup machines match (id = machine, geo = \"FR\")",
        "default_record from parts",
        "sum(parts match machine_id = machine, mass * count)",
        "sum(parts, mass)",
    )

    @Test
    fun print_shouldGiveTheExpressionAsWritten() {
        written.forEach { text -> assertEquals(text, printer.print(parse(text))) }
    }

    @Test
    fun print_thenParse_shouldGiveTheSameExpression() {
        written.forEach { text ->
            val expression = parse(text)
            assertEquals(expression, parse(printer.print(expression)), text)
        }
    }

    @Test
    fun print_shouldRemoveRedundantParentheses() {
        assertEquals("a + b * c", printer.print(parse("(a + (b * c))")))
        assertEquals("a * b", printer.print(parse("((a)) * (b)")))
        assertEquals("a * 2 b", printer.print(parse("a * (2 b)")))
    }

    private val kg = EUnitLiteral<BasicNumber>(UnitSymbol.of("kg"), 1.0, Dimension.of("mass"))
    private val kWh = EUnitLiteral<BasicNumber>(UnitSymbol.of("kWh"), 3.6e6, Dimension.of("energy"))
    private val percent = EUnitLiteral<BasicNumber>(UnitSymbol.of("percent"), 0.01, Dimension.None)
    private fun quantity(amount: Double, unit: EUnitLiteral<BasicNumber>) = EQuantityScale(BasicNumber(amount), unit)

    @Test
    fun scaledValues_shouldKeepTheValueOfTheExpression() {
        // A substituted value can put a scaled quantity where the grammar groups differently: the value must not change
        val evaluate = { text: String ->
            val values = mapOf("a" to 6.0, "b" to 3.0, "c" to 2.0)
            fun eval(e: DataExpression<BasicNumber>): Double = when (e) {
                is EDataRef -> values.getValue(e.name)
                is EQuantityScale -> e.scale.value * eval(e.base)
                is EQuantityMul -> eval(e.leftHandSide) * eval(e.rightHandSide)
                is EQuantityDiv -> eval(e.leftHandSide) / eval(e.rightHandSide)
                else -> throw IllegalArgumentException("$e")
            }
            eval(parse(text))
        }
        val a = EDataRef<BasicNumber>("a")
        val b = EDataRef<BasicNumber>("b")
        val c = EDataRef<BasicNumber>("c")
        val scaled = EQuantityScale(BasicNumber(2.0), b)
        // a = 6, b = 3, c = 2
        listOf(
            EQuantityDiv(EQuantityMul(a, scaled), c) to 6.0 * (2 * 3) / 2,
            EQuantityMul(EQuantityDiv(a, scaled), c) to 6.0 / (2 * 3) * 2,
            EQuantityMul(EQuantityScale(BasicNumber(2.0), a), c) to (2 * 6.0) * 2,
            EQuantityDiv(EQuantityScale(BasicNumber(2.0), a), c) to (2 * 6.0) / 2,
        ).forEach { (expression, expected) ->
            val text = printer.print(expression)
            assertEquals(expected, evaluate(text), 1e-9, text)
        }
    }

    @Test
    fun valueOf_shouldReplaceNamesAndRecordEntries() {
        // given
        val values = mapOf<String, DataExpression<BasicNumber>>(
            "mach" to ERecord(mapOf("consumption" to quantity(2.0, kWh), "runs" to quantity(3.0, percent))),
            "efficiency" to quantity(80.0, percent),
            "machine" to EStringLiteral("m1"),
        )
        val substituting = DataExpressionPrinter(BasicOperations) { values[it] }

        // then
        assertEquals(
            "2 kWh * 3 percent / (80 percent) * (1 u + grid_loss)",
            substituting.print(parse("mach.consumption * mach.runs / efficiency * (1 u + grid_loss)")),
        )
        assertEquals("lookup machines match id = \"m1\"", substituting.print(parse("lookup machines match id = machine")))
        assertEquals("{consumption: 2 kWh, runs: 3 percent}", substituting.print(parse("mach")))
    }

    @Test
    fun format_shouldBeUsedForNumbers() {
        val rounded = DataExpressionPrinter(BasicOperations, format = { String.format("%.2f", it) })
        assertEquals("0.33 kg", rounded.print(quantity(1.0 / 3.0, kg)))
    }

    @Test
    fun formatNumber() {
        assertEquals("2", formatNumber(2.0))
        assertEquals("-0.5", formatNumber(-0.5))
        assertEquals("1.0E-9", formatNumber(1e-9))
        assertEquals("1.0E15", formatNumber(1e15))
    }
}
