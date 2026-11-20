package ch.kleis.lcaac.cli.skills

import ch.kleis.lcaac.cli.skills.BundledSkills.Companion.SKILL_FILE
import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BundledSkillsTest {
    private val bundled = BundledSkills()

    @TempDir
    lateinit var directory: File

    @Test
    fun skills_shouldBeTheSkillsOfTheRepository() {
        assertEquals(listOf("lcaac-cli", "lcaac-modeling"), bundled.skills.map { it.name })
    }

    /** The constraints of https://agentskills.io/specification on the frontmatter and the size of `SKILL.md`. */
    @Test
    fun skills_shouldFollowTheSpecification() {
        bundled.skills.forEach { skill ->
            val content = bundled.read(skill.name, SKILL_FILE)
            val frontmatter = BundledSkills.frontmatter(content)
            val name = frontmatter["name"] ?: ""
            assertTrue(Regex("[a-z0-9]+(-[a-z0-9]+)*").matches(name) && name.length <= 64, "invalid name '$name'")
            assertTrue(content.contains("\n---"), "${skill.name}: no frontmatter")
            assertTrue(skill.description.length in 1..1024, "${skill.name}: description of ${skill.description.length} characters")
            assertTrue((frontmatter["compatibility"]?.length ?: 1) in 1..500, "${skill.name}: compatibility too long")
            assertTrue(BundledSkills.body(content).lines().size <= 500, "${skill.name}: SKILL.md over 500 lines")
            assertTrue(Regex("(?m)^\\s+lcaac-version: ").containsMatchIn(content), "${skill.name}: no lcaac-version")
        }
    }

    @Test
    fun skills_shouldBeNamedAfterTheirDirectory() {
        bundled.skills.forEach { skill ->
            assertEquals(skill.name, BundledSkills.frontmatter(bundled.read(skill.name, SKILL_FILE))["name"])
        }
    }

    @Test
    fun install_shouldWriteEachSkillWithTheVersionOfTheCli() {
        bundled.install(directory, "9.9.9")

        bundled.skills.forEach { skill ->
            skill.files.forEach { assertTrue(File(directory, "${skill.name}/$it").isFile, "${skill.name}/$it") }
            val installed = File(directory, "${skill.name}/$SKILL_FILE").readText()
            assertTrue(installed.contains("lcaac-version: \"9.9.9\""), installed)
            assertEquals(BundledSkills.body(bundled.read(skill.name, SKILL_FILE)), BundledSkills.body(installed))
        }
    }

    @Test
    fun install_whenInstalledBefore_shouldReplaceTheSkills() {
        bundled.install(directory, "1.0.0")
        val stale = File(directory, "lcaac-cli/stale.md").apply { writeText("stale") }

        bundled.install(directory, "2.0.0")

        assertFalse(stale.exists())
        assertTrue(File(directory, "lcaac-cli/$SKILL_FILE").readText().contains("lcaac-version: \"2.0.0\""))
    }

    @Test
    fun install_whenADirectoryOfTheSameNameIsNotAnLcaacSkill_shouldRefuseAndChangeNothing() {
        val mine = File(directory, "lcaac-cli/$SKILL_FILE").apply { parentFile.mkdirs(); writeText("mine") }

        assertFailsWith<SkillInstallException> { bundled.install(directory, "1.0.0") }

        assertEquals("mine", mine.readText())
        assertFalse(File(directory, "lcaac-modeling").exists())
    }
}
