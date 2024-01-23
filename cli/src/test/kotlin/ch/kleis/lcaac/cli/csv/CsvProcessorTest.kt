package ch.kleis.lcaac.cli.csv

import ch.kleis.lcaac.core.lang.SymbolTable
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import ch.kleis.lcaac.grammar.parser.LcaLangLexer
import ch.kleis.lcaac.grammar.parser.LcaLangParser
import io.mockk.mockk
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File

class CsvProcessorTest {
    @Test
    fun processWithRecordParam_withMissingColumn_shouldThrow() {
        // given
        val content = """
            datasource source {
                location = "source.csv"
                schema {
                    "quantity" = 10 u
                    "mass" = 1 kg
                }
            }
            process main {
                params {
                    row from source
                }
                products {
                    1 kWh electricity
                }
                impacts {
                    row["quantity"] * row["mass"] co2
                }
            }
        """.trimIndent()
        val symbolTable = load(content)
        val path = mockk<File>()
        val processor = CsvProcessor(path, symbolTable)
        val request = CsvRequest(
            "main",
            emptyMap(),
            header = mapOf("quantity" to 0),
            record = listOf("100.0")
        )

        // when
        val actual = processor.process(request)

        // then
        assertEquals(1, actual.size)
        assertEquals(request, actual[0].request)
        val kWh = UnitValue<BasicNumber>(UnitSymbol.of("kWh"), 1000.0, Dimension.Companion.of("energy"))
        val kg = UnitValue<BasicNumber>(UnitSymbol.of("kg"), 1.0, Dimension.Companion.of("mass"))
        val u = UnitValue<BasicNumber>(UnitSymbol.of("u"), 1.0, Dimension.None)
        assertEquals(
            ProductValue(
                name = "electricity",
                referenceUnit = kWh,
                fromProcessRef = FromProcessRefValue(
                    "main",
                    arguments = mapOf(
                        "row" to RecordValue(mapOf(
                            "quantity" to QuantityValue(BasicNumber(100.0), u),
                            "mass" to QuantityValue(BasicNumber(1.0), kg),
                        ))
                    ))
            ), actual[0].output
        )
        assertEquals(
            mapOf(
                IndicatorValue("co2", kg)
                    to QuantityValue(BasicNumber(100.0), kg)
            ),
            actual[0].impacts)
    }

    @Test
    fun processWithRecordParam_whenCsvRequest() {
        // given
        val content = """
            datasource source {
                location = "source.csv"
                schema {
                    "quantity" = 10 u
                    "mass" = 1 kg
                }
            }
            process main {
                params {
                    row from source
                }
                products {
                    1 kWh electricity
                }
                impacts {
                    row["quantity"] * row["mass"] co2
                }
            }
        """.trimIndent()
        val symbolTable = load(content)
        val path = mockk<File>()
        val processor = CsvProcessor(path, symbolTable)
        val request = CsvRequest(
            "main",
            emptyMap(),
            header = mapOf("quantity" to 0, "mass" to 1),
            record = listOf("100.0", "2.0")
        )

        // when
        val actual = processor.process(request)

        // then
        assertEquals(1, actual.size)
        assertEquals(request, actual[0].request)
        val kWh = UnitValue<BasicNumber>(UnitSymbol.of("kWh"), 1000.0, Dimension.Companion.of("energy"))
        val kg = UnitValue<BasicNumber>(UnitSymbol.of("kg"), 1.0, Dimension.Companion.of("mass"))
        val u = UnitValue<BasicNumber>(UnitSymbol.of("u"), 1.0, Dimension.None)
        assertEquals(
            ProductValue(
                name = "electricity",
                referenceUnit = kWh,
                fromProcessRef = FromProcessRefValue(
                    "main",
                    arguments = mapOf(
                        "row" to RecordValue(mapOf(
                            "quantity" to QuantityValue(BasicNumber(100.0), u),
                            "mass" to QuantityValue(BasicNumber(2.0), kg),
                        ))
                    ))
            ), actual[0].output
        )
        assertEquals(
            mapOf(
                IndicatorValue("co2", kg)
                    to QuantityValue(BasicNumber(200.0), kg)
            ),
            actual[0].impacts)
    }

    @Test
    fun process() {
        // given
        val content = """
            process main {
                params {
                    x = 1 u
                products {
                    1 kWh electricity
                }
                impacts {
                    x * 1 kg co2
                }
            }
        """.trimIndent()
        val symbolTable = load(content)
        val path = mockk<File>()
        val processor = CsvProcessor(path, symbolTable)
        val request = CsvRequest(
            "main",
            emptyMap(),
            header = mapOf("id" to 0, "x" to 1),
            record = listOf("id-0", "0.5")
        )

        // when
        val actual = processor.process(request)

        // then
        assertEquals(1, actual.size)
        assertEquals(request, actual[0].request)
        val kWh = UnitValue<BasicNumber>(UnitSymbol.of("kWh"), 1000.0, Dimension.Companion.of("energy"))
        val kg = UnitValue<BasicNumber>(UnitSymbol.of("kg"), 1.0, Dimension.Companion.of("mass"))
        val u = UnitValue<BasicNumber>(UnitSymbol.of("u"), 1.0, Dimension.None)
        assertEquals(
            ProductValue(
                name = "electricity",
                referenceUnit = kWh,
                fromProcessRef = FromProcessRefValue(
                    "main",
                    arguments = mapOf(
                        "x" to QuantityValue(BasicNumber(0.5), u)
                    ))
            ), actual[0].output
        )
        assertEquals(
            mapOf(
                IndicatorValue("co2", kg)
                    to QuantityValue(BasicNumber(0.5), kg)
            ),
            actual[0].impacts)
    }

    private fun load(content: String): SymbolTable<BasicNumber> {
        val lexer = LcaLangLexer(CharStreams.fromStream(content.byteInputStream()))
        val tokens = CommonTokenStream(lexer)
        val parser = LcaLangParser(tokens)
        val file = parser.lcaFile()
        val loader = Loader(BasicOperations)
        return loader.load(sequenceOf(file), listOf(LoaderOption.WITH_PRELUDE))
    }
}
