package ch.kleis.lcaac.core.lang.evaluator

import ch.kleis.lcaac.core.lang.register.ProcessKey
import ch.kleis.lcaac.core.lang.register.SubstanceKey
import ch.kleis.lcaac.core.lang.source.*
import ch.kleis.lcaac.core.testing.TestCase
import kotlin.test.Test
import kotlin.test.assertEquals

class EvaluatorExceptionTest {
    private val process = ProcessKey("p")
    private val forEach = SourcePath.Exchange(SourcePath.Process(process), BlockPath.of(Section.INPUTS, 0))

    @Test
    fun describe_whenNoFrame_shouldBeTheMessage() {
        assertEquals("boom", EvaluatorException("boom").describe())
    }

    @Test
    fun describe_shouldListFramesWithKnownLocations() {
        // given
        val error = EvaluatorException("boom")
            .within(VariableFrame(SourcePath.Variable(SourcePath.Process(process), "x")))
            .within(ProcessFrame(SourcePath.Process(process)))
            .withSourceMap(SourceMap(mapOf(SourcePath.Process(process) to SourceLocation("main.lca", 3, 1))))

        // then
        assertEquals(
            """
            boom
              in variable 'x'
              in process 'p' (main.lca:3:1)
            """.trimIndent(),
            error.describe(),
        )
    }

    @Test
    fun within_whenSamePathAsOutermostFrame_shouldNotRepeatIt() {
        // given
        val error = EvaluatorException("boom")
            .within(ForEachFrame(forEach, "row", 1))
            .within(ForEachFrame(forEach, "row"))
            .within(ProcessFrame(SourcePath.Process(process)))

        // then
        assertEquals(
            listOf(ForEachFrame(forEach, "row", 1), ProcessFrame(SourcePath.Process(process))),
            error.frames,
        )
    }

    @Test
    fun describe_shouldListRelatedDefinitionsAfterTheFrames() {
        // given
        val co2 = SubstanceKey("co2")
        val error = EvaluatorException("boom")
            .within(ProcessFrame(SourcePath.Process(process)))
            .seeAlso(ReferenceUnitFrame(SourcePath.ReferenceUnit(co2)))
            .withSourceMap(SourceMap(mapOf(SourcePath.ReferenceUnit(co2) to SourceLocation("main.lca", 14, 5))))

        // then
        assertEquals(
            """
            boom
              in process 'p'
              see reference unit of substance 'co2' (main.lca:14:5)
            """.trimIndent(),
            error.describe(),
        )
    }

    @Test
    fun processGeneratedForATest_shouldBeDescribedAsTheTest() {
        val test = ProcessKey(TestCase.processName("my_test"))
        assertEquals("test 'my_test'", ProcessFrame(SourcePath.Process(test)).description)
        assertEquals("supplier of 'flour' for test 'my_test'", SupplierFrame(SourcePath.Process(test), "flour", test).description)
    }

    @Test
    fun frameDescriptions() {
        assertEquals("input 'flour'", ExchangeFrame(forEach, "input", "flour").description)
        assertEquals("row #2 of for_each 'row'", ForEachFrame(forEach, "row", 1).description)
        assertEquals("for_each 'row'", ForEachFrame(forEach, "row").description)
        assertEquals("parameter 'a'", VariableFrame(SourcePath.Parameter(process, "a")).description)
        assertEquals("global 'g'", VariableFrame(SourcePath.Global("g")).description)
        assertEquals("supplier of 'flour' for process 'p'", SupplierFrame(forEach, "flour", process).description)
        assertEquals("substance 'co2'", SubstanceFrame(SourcePath.Substance(SubstanceKey("co2"))).description)
        assertEquals(
            "characterization of 'co2' for process 'p'",
            CharacterizationFrame(SourcePath.Process(process), "co2", process).description,
        )
        assertEquals("reference unit of substance 'co2'", ReferenceUnitFrame(SourcePath.ReferenceUnit(SubstanceKey("co2"))).description)
        assertEquals("product 'steel' of process 'p'", ProductFrame(SourcePath.Process(process), "steel", process).description)
    }
}
