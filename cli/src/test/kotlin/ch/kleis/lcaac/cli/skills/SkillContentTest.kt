package ch.kleis.lcaac.cli.skills

import ch.kleis.lcaac.grammar.parseLcaText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * The content of the skills must stay true to the CLI and the language: the command reference is the help of the
 * commands, and every `lca` block parses.
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
        val blocks = bundled.skills.flatMap { skill ->
            skill.files.filter { it.endsWith(".md") }.flatMap { file ->
                lcaBlock.findAll(bundled.read(skill.name, file)).map { "${skill.name}/$file" to it.groupValues[1] }
            }
        }
        assertTrue(blocks.isNotEmpty())

        blocks.forEachIndexed { index, (file, block) ->
            parseLcaText(block, "$file (block ${index + 1})")
        }
    }

    private val lcaBlock = Regex("```lca\\R(.*?)\\R```", RegexOption.DOT_MATCHES_ALL)
}
