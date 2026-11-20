package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.cli.skills.BundledSkills
import ch.kleis.lcaac.cli.skills.SkillInstallException
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.NoOpCliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import java.io.File
import java.util.Properties

class SkillsCommand : NoOpCliktCommand(
    name = "skills",
    help = "Agent skills for lcaac (https://agentskills.io), to help AI agents write models and use the CLI",
)

class ListSkillsCommand : CliktCommand(name = "list", help = "List the skills bundled with this version of lcaac") {
    override fun run() {
        BundledSkills().skills.forEach { echo("${it.name}: ${it.description}") }
    }
}

class InstallSkillsCommand : CliktCommand(
    name = "install",
    help = "Install the skills bundled with this version of lcaac, by default into .agents/skills of the " +
        "current directory, where agents look for the skills of a project. Skills installed by lcaac before " +
        "are replaced.",
) {
    private val user: Boolean by option("--user", help = "Install into ~/.agents/skills, for all the projects of the user")
        .flag()
    private val directory: File? by option(
        "--dir", help = "Install into this directory, e.g. .claude/skills for an agent with its own directory",
    ).file(canBeFile = false)

    override fun run() {
        if (user && directory != null) throw CliktError("--user and --dir cannot be used together")
        val target = directory ?: if (user) File(System.getProperty("user.home"), DEFAULT_DIRECTORY) else File(DEFAULT_DIRECTORY)
        val skills = try {
            BundledSkills().install(target, cliVersion())
        } catch (e: SkillInstallException) {
            throw CliktError(e.message)
        }
        echo("Installed ${skills.joinToString(", ") { it.name }} into ${target.path}")
    }

    private companion object {
        const val DEFAULT_DIRECTORY = ".agents/skills"
    }
}

private fun cliVersion(): String =
    {}.javaClass.classLoader.getResourceAsStream("META-INF/lcaac.properties").use {
        Properties().apply { load(it) }
    }.getProperty("version")
