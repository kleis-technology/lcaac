package ch.kleis.lcaac.cli.skills

import ch.kleis.lcaac.cli.lcaacCommand
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.PrintHelpMessage
import com.github.ajalt.clikt.core.context
import com.github.ajalt.mordant.rendering.AnsiLevel
import com.github.ajalt.mordant.terminal.Terminal
import java.io.File

/**
 * The help of every command of the CLI, as Markdown: the command reference of the skill `lcaac-cli`. Rendered
 * without colors and at a fixed width, so that it does not depend on the terminal.
 */
fun commandReference(): String {
    fun paths(command: CliktCommand, path: List<String>): List<List<String>> =
        listOf(path) + command.registeredSubcommands().flatMap { paths(it, path + it.commandName) }

    val sections = paths(lcaacCommand(), emptyList()).map { path ->
        val help = helpOf(path)
        "## ${(listOf("lcaac") + path).joinToString(" ")}\n\n```text\n${help.trimEnd()}\n```\n"
    }
    return "# lcaac commands\n\n" +
        "The help of every command, as printed by `lcaac <command> --help`. Generated: do not edit.\n\n" +
        sections.joinToString("\n")
}

private fun helpOf(path: List<String>): String {
    val command = lcaacCommand().context {
        terminal = Terminal(ansiLevel = AnsiLevel.NONE, width = 100, interactive = false)
    }
    return try {
        command.parse(path + "--help")
        throw IllegalStateException("no help for ${path.joinToString(" ")}")
    } catch (e: PrintHelpMessage) {
        e.context?.command?.getFormattedHelp() ?: throw IllegalStateException("no help for ${path.joinToString(" ")}")
    }
}

/** Writes the command reference to the file given as argument. */
fun main(args: Array<String>) {
    File(args.single()).writeText(commandReference())
}
