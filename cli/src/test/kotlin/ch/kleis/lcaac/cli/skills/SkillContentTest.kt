package ch.kleis.lcaac.cli.skills

import ch.kleis.lcaac.core.config.LcaacConfig
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.core.testing.BasicTestRunner
import ch.kleis.lcaac.core.testing.RangeAssertionSuccess
import ch.kleis.lcaac.grammar.CoreTestMapper
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import ch.kleis.lcaac.grammar.parseLcaText
import ch.kleis.lcaac.grammar.parser.LcaLangParser
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * The content of the skills must stay true to the CLI and the language: the command reference is the help of the
 * commands, every `lca` block parses, and the tests of the blocks that have some pass.
 */
class SkillContentTest {
    private val bundled = BundledSkills()

    @Test
    fun commandReference_shouldBeTheHelpOfTheCommands() {
        assertEquals(
            commandReference(),
            bundled.read("lcaac-cli", "references/commands.md"),
            "skills/lcaac-cli/references/commands.md is out of date: run ./gradlew :cli:writeCommandReference",
        )
    }

    @Test
    fun lcaBlocks_shouldParse() {
        val blocks = lcaBlocks()
        assertTrue(blocks.isNotEmpty())

        blocks.forEach { (name, block) -> parseLcaText(block, name) }
    }

    /** A block with tests is a complete model on its own, without data, and its tests pass. */
    @Test
    fun lcaBlocksWithTests_shouldPass() {
        val blocks = lcaBlocks().filter { (_, block) -> Regex("(?m)^test ").containsMatchIn(block) }
        assertTrue(blocks.isNotEmpty())

        blocks.forEach { (name, block) ->
            val file = parseLcaText(block, name)
            val symbolTable = Loader(BasicOperations).load(sequenceOf(file), listOf(LoaderOption.WITH_PRELUDE))
            val runner = BasicTestRunner<LcaLangParser.TestDefinitionContext>(
                symbolTable, DefaultDataSourceOperations(BasicOperations, LcaacConfig(), emptyMap()),
            )
            file.testDefinition().map { CoreTestMapper().test(it) }.forEach { case ->
                runner.run(case).results.forEach { result ->
                    assertIs<RangeAssertionSuccess>(result, "$name: test '${case.name}': $result")
                }
            }
        }
    }

    /** The `lca` blocks of the skills, named after their file and position. */
    private fun lcaBlocks(): List<Pair<String, String>> = bundled.skills.flatMap { skill ->
        skill.files.filter { it.endsWith(".md") }.flatMap { file ->
            lcaBlock.findAll(bundled.read(skill.name, file)).mapIndexed { index, match ->
                "${skill.name}/$file (block ${index + 1})" to match.groupValues[1]
            }
        }
    }

    private val lcaBlock = Regex("```lca\\R(.*?)\\R```", RegexOption.DOT_MATCHES_ALL)
}
