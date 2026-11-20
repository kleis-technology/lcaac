package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.assessment.TraceRow
import ch.kleis.lcaac.core.assessment.TraceTableProgram
import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.lang.evaluator.Evaluator
import ch.kleis.lcaac.core.lang.value.ProductValue
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TraceTableTest {
    private val ops = BasicOperations

    private fun table(content: String, process: String): List<TraceRow> {
        val symbolTable = Loader(ops).load(sequenceOf(LcaLangFixture.parser(content.trimIndent()).lcaFile()), listOf(LoaderOption.WITH_PRELUDE))
        val sourceOps = mockk<DataSourceOperations<BasicNumber>>()
        val trace = Evaluator(symbolTable, ops, sourceOps).trace(assertNotNull(symbolTable.getTemplate(process)))
        return TraceTableProgram(trace).run()
    }

    @Test
    fun run_shouldListTheProductsOfTheSupplyChain() {
        // given
        val content = """
            process bakery {
                products {
                    1 kg bread
                }
                inputs {
                    0.8 kg flour
                    0.5 kWh electricity
                }
                impacts {
                    0.05 kg_CO2_Eq climate_change
                }
            }
            process mill {
                products {
                    1 kg flour
                }
                inputs {
                    1.2 kg wheat
                    0.2 kWh electricity
                }
            }
            process farm {
                products {
                    1 kg wheat
                }
                impacts {
                    0.4 kg_CO2_Eq climate_change
                }
            }
            process power_plant {
                products {
                    1 kWh electricity
                }
                impacts {
                    0.1 kg_CO2_Eq climate_change
                }
            }
        """

        // when
        val actual = table(content, "bakery")

        // then
        val rows = actual.map {
            val product = it.output as ProductValue<BasicNumber>
            val impact = it.impacts.entries.single { e -> e.key.getShortName() == "climate_change" }.value
            listOf(it.depth, product.name, product.fromProcessRef?.name, it.supply.amount.value, impact.amount.value)
        }
        assertEquals(
            listOf(
                listOf(0, "bread", "bakery", 1.0, 0.5),
                listOf(1, "flour", "mill", 0.8, 0.4),
                listOf(2, "electricity", "power_plant", 0.66, 0.066),
                listOf(2, "wheat", "farm", 0.96, 0.384),
            ),
            rows.map { row -> row.map { if (it is Double) Math.round(it * 1e6) / 1e6 else it } },
        )
        assertEquals(setOf("bread"), actual.map { it.demandedProduct.product.name }.toSet())
    }
}
