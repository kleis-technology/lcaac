package ch.kleis.lcaac.grammar

import ch.kleis.lcaac.core.datasource.DataSourceOperations
import ch.kleis.lcaac.core.math.basic.BasicNumber
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.core.testing.BasicTestRunner
import ch.kleis.lcaac.core.testing.GenericFailure
import ch.kleis.lcaac.core.testing.RangeAssertionFailure
import ch.kleis.lcaac.core.testing.RangeAssertionSuccess
import ch.kleis.lcaac.grammar.parser.LcaLangParser
import io.mockk.mockk
import java.nio.file.Files
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull

class TestErrorLocationTest {
    // Line numbers are used in the assertions: keep the content as is
    private val content = """
        process steel_mill {
            products {
                1 kg steel
            }
        }

        test bad_variable {
            variables {
                x = 1 kg + 1 kWh
            }
            given {
                x steel
            }
            assert {
                steel between 0 kg and 10 kg
            }
        }

        test wrong_supplier {
            given {
                1 kg iron from steel_mill
            }
            assert {
                iron between 0 kg and 10 kg
            }
        }

        test assertions {
            given {
                12 kg steel
            }
            assert {
                steel between 0 kg and 10 kg
                steel between 1 kg + 1 kWh and 20 kg
                steel between 10 kg and 20 kg
            }
        }
    """.trimIndent()

    private val path = Files.createTempFile("tests", ".lca").also { Files.writeString(it, content) }
    private val file = parseLcaFile(path)
    private val symbolTable = Loader(BasicOperations).load(sequenceOf(file), listOf(LoaderOption.WITH_PRELUDE))
    private val runner = BasicTestRunner<LcaLangParser.TestDefinitionContext>(symbolTable, mockk<DataSourceOperations<BasicNumber>>())

    private fun case(test: String) = file.testDefinition().map { CoreTestMapper().test(it) }.first { it.name == test }

    private fun describe(test: String): String {
        val failure = assertIs<GenericFailure>(runner.run(case(test)).results.single())
        return assertNotNull(failure.error).describe()
    }

    @Test
    fun assertions_shouldBeLocated() {
        val case = case("assertions")
        assertEquals(
            listOf(33, 34, 35),
            case.assertions.map { assertNotNull(case.sourceMap[assertNotNull(it.path)]).line },
        )
    }

    @Test
    fun errorInAssertion_shouldBeLocatedAndOnlyFailThisAssertion() {
        val results = runner.run(case("assertions")).results

        assertIs<RangeAssertionFailure>(results[0])
        assertEquals(
            """
            incompatible dimensions: mass vs energy in left=kg and right=kWh
              in assertion on 'steel' ($path:34:9)
            """.trimIndent(),
            assertNotNull(assertIs<GenericFailure>(results[1]).error).describe(),
        )
        assertIs<RangeAssertionSuccess>(results[2])
    }

    @Test
    fun variableOfTest_shouldBeLocatedInTheTest() {
        assertEquals(
            """
            incompatible dimensions: mass vs energy in left=kg and right=kWh
              in variable 'x' ($path:9:9)
              in input 'steel' ($path:12:9)
              in test 'bad_variable' ($path:7:1)
            """.trimIndent(),
            describe("bad_variable"),
        )
    }

    @Test
    fun supplierOfTest_shouldBeLocatedAtTheGivenInput() {
        assertEquals(
            """
            no process 'steel_mill' providing 'iron' found
              in supplier of 'iron' for test 'wrong_supplier' ($path:21:9)
            """.trimIndent(),
            describe("wrong_supplier"),
        )
    }
}
