package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.core.lang.evaluator.ForEachFrame
import ch.kleis.lcaac.core.lang.expression.EQuantityScale
import ch.kleis.lcaac.core.lang.expression.ERecord
import ch.kleis.lcaac.core.lang.expression.EStringLiteral
import ch.kleis.lcaac.core.lang.expression.EUnitLiteral
import ch.kleis.lcaac.core.lang.dimension.Dimension
import ch.kleis.lcaac.core.lang.dimension.UnitSymbol
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.assertThrows
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class EvaluationErrorLocationTest {
    private val ops = BasicOperations

    private fun kg(amount: Double) = EQuantityScale(BasicNumber(amount), EUnitLiteral(UnitSymbol.of("kg"), 1.0, Dimension.of("mass")))
    private fun kWh(amount: Double) = EQuantityScale(BasicNumber(amount), EUnitLiteral(UnitSymbol.of("kWh"), 3.6e6, Dimension.of("energy")))

    private val sourceOps = mockk<DataSourceOperations<BasicNumber>>().also {
        // row #1 is consistent, row #2 mixes mass and energy
        every { it.getAll(any()) } returns sequenceOf(
            ERecord(mapOf("id" to EStringLiteral("p1"), "mass" to kWh(1.0))),
            ERecord(mapOf("id" to EStringLiteral("p2"), "mass" to kg(2.0))),
        )
    }

    private fun evaluate(content: String, process: String): Pair<EvaluatorException, String> {
        val path = Files.createTempFile("errors", ".lca").also { Files.writeString(it, content.trimIndent()) }
        val symbolTable = Loader(ops).load(sequenceOf(parseLcaFile(path)), listOf(LoaderOption.WITH_PRELUDE))
        val template = assertNotNull(symbolTable.getTemplate(process))
        val error = assertThrows<EvaluatorException> { Evaluator(symbolTable, ops, sourceOps).trace(template) }
        return error to path.toString()
    }

    @Test
    fun variableOfSupplier_shouldListTheChainOfProcesses() {
        val (error, file) = evaluate(
            """
            process main {
                products {
                    1 p widget
                }
                inputs {
                    2 kg steel
                }
            }

            process steel_mill {
                variables {
                    energy = 3 kWh + 1 kg
                }
                products {
                    1 kg steel
                }
                inputs {
                    energy electricity
                }
            }
            """, "main"
        )

        assertEquals(
            """
            incompatible dimensions: energy vs mass in left=kWh and right=kg
              in variable 'energy' ($file:12:9)
              in input 'electricity' ($file:18:9)
              in process 'steel_mill' ($file:10:1)
              in supplier of 'steel' for process 'main' ($file:6:9)
            """.trimIndent(),
            error.describe(),
        )
    }

    @Test
    fun globalVariable() {
        val (error, file) = evaluate(
            """
            variables {
                loss = 1 kg + 1 kWh
            }

            process p {
                products {
                    1 p a
                }
                emissions {
                    loss co2
                }
            }
            """, "p"
        )

        assertEquals(
            """
            incompatible dimensions: mass vs energy in left=kg and right=kWh
              in global 'loss' ($file:2:5)
              in emission 'co2' ($file:10:9)
              in process 'p' ($file:5:1)
            """.trimIndent(),
            error.describe(),
        )
    }

    @Test
    fun forEach_shouldGiveTheRowOnce() {
        val (error, file) = evaluate(
            """
            datasource parts {
                location = "parts.csv"
                schema {
                    id = "p1"
                    mass = 1 kg
                }
            }

            process p {
                products {
                    1 p a
                }
                impacts {
                    for_each part from parts {
                        variables {
                            wrap = part.mass + 1 kWh
                        }
                        wrap climate_change
                    }
                }
            }
            """, "p"
        )

        assertEquals(
            """
            incompatible dimensions: mass vs energy in left=kg and right=kWh
              in variable 'wrap' ($file:16:17)
              in impact 'climate_change' ($file:18:13)
              in row #2 of for_each 'part' ($file:14:9)
              in process 'p' ($file:9:1)
            """.trimIndent(),
            error.describe(),
        )
        assertEquals(1, error.frames.count { it is ForEachFrame })
    }

    @Test
    fun supplierNotProvidingTheProduct() {
        val (error, file) = evaluate(
            """
            process main {
                products {
                    1 p widget
                }
                inputs {
                    1 kg steel from other
                }
            }

            process other {
                products {
                    1 kg iron
                }
            }
            """, "main"
        )

        assertEquals(
            """
            no process 'other' providing 'steel' found
              in supplier of 'steel' for process 'main' ($file:6:9)
            """.trimIndent(),
            error.describe(),
        )
    }

    @Test
    fun substanceCharacterization() {
        val (error, file) = evaluate(
            """
            process p {
                products {
                    1 p a
                }
                emissions {
                    1 kg co2(compartment = "air")
                }
            }

            substance co2 {
                name = "co2"
                type = Emission
                compartment = "air"
                reference_unit = kg
                impacts {
                    1 kg + 1 kWh climate_change
                }
            }
            """, "p"
        )

        assertEquals(
            """
            incompatible dimensions: mass vs energy in left=kg and right=kWh
              in impact 'climate_change' ($file:16:9)
              in substance 'co2' ($file:10:1)
              in characterization of 'co2' for process 'p' ($file:6:9)
            """.trimIndent(),
            error.describe(),
        )
    }

    @Test
    fun parameterOfSupplier() {
        val (error, file) = evaluate(
            """
            process main {
                products {
                    1 p widget
                }
                inputs {
                    1 kg steel from mill(mass = 1 kWh)
                }
            }

            process mill {
                params {
                    mass = 1 kg
                }
                variables {
                    total = mass + 1 kg
                }
                products {
                    1 kg steel
                }
                inputs {
                    total iron
                }
            }
            """, "main"
        )

        assertEquals(
            """
            incompatible dimensions: energy vs mass in left=kWh and right=kg
              in variable 'total' ($file:15:9)
              in input 'iron' ($file:21:9)
              in process 'mill' ($file:10:1)
              in supplier of 'steel' for process 'main' ($file:6:9)
            """.trimIndent(),
            error.describe(),
        )
    }
}
