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
        every { it.getFirst(any()) } throws EvaluatorException("no record found in datasource 'broken'")
        every { it.getAll(any()) } returns sequenceOf(
            ERecord(mapOf("id" to EStringLiteral("p1"), "mass" to kWh(1.0))),
            ERecord(mapOf("id" to EStringLiteral("p2"), "mass" to kg(2.0))),
        )
    }

    private fun evaluate(
        content: String,
        process: String,
        labels: Map<String, String> = emptyMap(),
    ): Pair<EvaluatorException, String> {
        val path = Files.createTempFile("errors", ".lca").also { Files.writeString(it, content.trimIndent()) }
        val symbolTable = Loader(ops).load(sequenceOf(parseLcaFile(path)), listOf(LoaderOption.WITH_PRELUDE))
        val template = assertNotNull(symbolTable.getTemplate(process, labels))
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
    fun referenceUnitOfSubstance() {
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
                reference_unit = 1 kg + 1 kWh
                impacts {
                    1 u climate_change
                }
            }
            """, "p"
        )

        assertEquals(
            """
            incompatible dimensions: mass vs energy in left=kg and right=kWh
              in reference unit of substance 'co2' ($file:14:5)
              in substance 'co2' ($file:10:1)
              in characterization of 'co2' for process 'p' ($file:6:9)
            """.trimIndent(),
            error.describe(),
        )
    }

    @Test
    fun emissionNotMatchingTheReferenceUnit_shouldReferToIt() {
        val (error, file) = evaluate(
            """
            process p {
                products {
                    1 p a
                }
                emissions {
                    1 kWh co2(compartment = "air")
                }
            }

            substance co2 {
                name = "co2"
                type = Emission
                compartment = "air"
                reference_unit = kg
                impacts {
                    1 u climate_change
                }
            }
            """, "p"
        )

        assertEquals(
            listOf(
                "  in emission 'co2' ($file:6:9)",
                "  in process 'p' ($file:1:1)",
                "  see reference unit of substance 'co2' ($file:14:5)",
            ),
            error.describe().lines().drop(1),
        )
    }

    @Test
    fun inputNotMatchingTheSupplierProduct_shouldReferToIt() {
        val (error, file) = evaluate(
            """
            process main {
                products {
                    1 p widget
                }
                inputs {
                    2 kWh steel
                }
            }

            process steel_mill {
                products {
                    1 kg steel
                }
            }
            """, "main"
        )

        assertEquals(
            """
            incompatible dimensions: energy vs mass for product steel
              in input 'steel' ($file:6:9)
              in process 'main' ($file:1:1)
              see product 'steel' of process 'steel_mill' ($file:10:1)
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

    /*
        Corner cases
     */

    private fun framesOf(error: EvaluatorException) = error.describe().lines().drop(1)

    @Test
    fun processWithLabels_shouldShowTheLabels() {
        val (error, file) = evaluate(
            """
            process p {
                labels {
                    geo = "FR"
                }
                variables {
                    x = 1 kg + 1 kWh
                }
                products {
                    1 p a
                }
                inputs {
                    x steel
                }
            }
            """, "p", mapOf("geo" to "FR")
        )

        assertEquals(
            listOf(
                "  in variable 'x' ($file:6:9)",
                "  in input 'steel' ($file:12:9)",
                "  in process 'p{geo=FR}' ($file:1:1)",
            ),
            framesOf(error),
        )
    }

    @Test
    fun nestedForEach_shouldGiveTheRowOfEachBlock() {
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
                inputs {
                    for_each outer from parts {
                        for_each inner from parts {
                            variables {
                                w = outer.mass * inner.mass + 1 kWh
                            }
                            w packaging
                        }
                    }
                }
            }
            """, "p"
        )

        assertEquals(
            listOf(
                "  in variable 'w' ($file:17:21)",
                "  in input 'packaging' ($file:19:17)",
                "  in row #1 of for_each 'inner' ($file:15:13)",
                "  in row #1 of for_each 'outer' ($file:14:9)",
                "  in process 'p' ($file:9:1)",
            ),
            framesOf(error),
        )
    }

    @Test
    fun errorInsideCachedProcess() {
        val (error, file) = evaluate(
            """
            process main {
                products {
                    1 p a
                }
                inputs {
                    1 kg steel
                }
            }

            @cached
            process steel_mill {
                variables {
                    e = 1 kWh + 1 kg
                }
                products {
                    1 kg steel
                }
                inputs {
                    e electricity
                }
            }
            """, "main"
        )

        assertEquals(
            listOf(
                "  in variable 'e' ($file:13:9)",
                "  in input 'electricity' ($file:19:9)",
                "  in process 'steel_mill' ($file:11:1)",
                "  in supplier of 'steel' for process 'main' ($file:6:9)",
            ),
            framesOf(error),
        )
    }

    @Test
    fun supplierErrorInsideCachedProcess() {
        val (error, file) = evaluate(
            """
            process main {
                products {
                    1 p a
                }
                inputs {
                    1 kg steel
                }
            }

            @cached
            process steel_mill {
                products {
                    1 kg steel
                }
                inputs {
                    1 kg iron from other
                }
            }

            process other {
                products {
                    1 kg copper
                }
            }
            """, "main"
        )

        assertEquals(
            """
            no process 'other' providing 'iron' found
              in supplier of 'iron' for process 'steel_mill' ($file:16:9)
              in supplier of 'steel' for process 'main' ($file:6:9)
            """.trimIndent(),
            error.describe(),
        )
    }

    @Test
    fun errorInALoop() {
        val (error, file) = evaluate(
            """
            process main {
                products {
                    1 p a
                }
                inputs {
                    1 kg steel
                }
            }

            process steel_mill {
                products {
                    1 kg steel
                }
                inputs {
                    3 kWh electricity
                }
            }

            process power_plant {
                variables {
                    sv = 0.1 kg + 1 kWh
                }
                products {
                    1 kWh electricity
                }
                inputs {
                    sv steel
                }
            }
            """, "main"
        )

        assertEquals(
            listOf(
                "  in variable 'sv' ($file:21:9)",
                "  in input 'steel' ($file:27:9)",
                "  in process 'power_plant' ($file:19:1)",
                "  in supplier of 'electricity' for process 'steel_mill' ($file:15:9)",
            ),
            framesOf(error),
        )
    }

    @Test
    fun sameProductFromTwoSuppliers_shouldLocateTheRightInput() {
        val (error, file) = evaluate(
            """
            process main {
                products {
                    1 p a
                }
                inputs {
                    1 kg steel from mill_a
                    1 kWh steel from mill_b
                }
            }

            process mill_a {
                products {
                    1 kg steel
                }
            }

            process mill_b {
                products {
                    1 kg steel
                }
            }
            """, "main"
        )

        assertEquals(
            """
            incompatible dimensions: energy vs mass for product steel
              in input 'steel' ($file:7:9)
              in process 'main' ($file:1:1)
              see product 'steel' of process 'mill_b' ($file:17:1)
            """.trimIndent(),
            error.describe(),
        )
    }

    @Test
    fun unknownParameter_shouldBeLocatedAtTheInput() {
        val (error, file) = evaluate(
            """
            process main {
                products {
                    1 p a
                }
                inputs {
                    1 kg steel from mill(nope = 2 kg)
                }
            }

            process mill {
                products {
                    1 kg steel
                }
            }
            """, "main"
        )

        assertEquals(
            """
            unknown parameters: [nope]
              in process 'mill' ($file:10:1)
              in supplier of 'steel' for process 'main' ($file:6:9)
            """.trimIndent(),
            error.describe(),
        )
    }

    @Test
    fun errorOfTheDataSource_shouldBeLocatedAtTheLookup() {
        val (error, file) = evaluate(
            """
            datasource broken {
                location = "broken.csv"
                schema {
                    id = "p1"
                    mass = 1 kg
                }
            }

            process p {
                variables {
                    rec = lookup broken match id = "zzz"
                }
                products {
                    1 p a
                }
                inputs {
                    rec.mass steel
                }
            }
            """, "p"
        )

        assertEquals(
            """
            no record found in datasource 'broken'
              in variable 'rec' ($file:11:9)
              in input 'steel' ($file:17:9)
              in process 'p' ($file:9:1)
            """.trimIndent(),
            error.describe(),
        )
    }

    @Test
    fun recursiveUnits_shouldListTheUnits() {
        val (error, file) = evaluate(
            """
            unit foo {
                symbol = "foo"
                alias_for = 2 bar
            }

            unit bar {
                symbol = "bar"
                alias_for = 3 foo
            }

            process p {
                products {
                    1 p a
                }
                inputs {
                    1 foo steel
                }
            }
            """, "p"
        )

        assertEquals(
            """
            Recursive dependency for unit foo
              in global 'foo' ($file:1:1)
              in global 'bar' ($file:6:1)
              in global 'foo' ($file:1:1)
              in input 'steel' ($file:16:9)
              in process 'p' ($file:11:1)
            """.trimIndent(),
            error.describe(),
        )
    }
}
