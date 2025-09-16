package ch.kleis.lcaac.cli.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.associate
import com.github.ajalt.clikt.parameters.options.convert
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.help
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.file
import java.io.File
import kotlin.io.path.Path

fun CliktCommand.configFileOption() = option("-c", "--config", help = "Path to LCAAC config file")
    .file(canBeDir = false)
    .convert { it.absoluteFile } // allow to retrieve its parent file when a relative path is given (useful for finding the project directory)
    .default(File("./$defaultLcaacFilename"))
    .help("""
        Path to LCAAC config file. Defaults to 'lcaac.yaml'
        The location of the config file is the project directory. Defaults to current working directory.
        """.trimIndent())

fun CliktCommand.sourceOption() = option("-s", "--source", help = "Path to LCA source folder or zip/tar.gz/tgz file")
    .file()
    .default(Path(".").toFile())
    .help("Path to LCAAC source folder or zip/tar.gz/tgz file. Defaults to current working directory.")

fun CliktCommand.fileOption(commandName: String) = option("-f", "--file")
    .file(canBeDir = false)
    .help("""
            CSV file with parameter values.
            Example: `lcaac $commandName <process name> -f params.csv`
        """.trimIndent()
    )

fun CliktCommand.labelsOption(commandName: String) = option("-l", "--label")
    .help("""
                Specify a process label as a key value pair.
                Example: lcaac $commandName <process name> -l model="ABC" -l geo="FR".
            """.trimIndent())
    .associate()

fun CliktCommand.argumentsOption(commandName: String) = option("-D", "--parameter")
    .help(
        """
                Override parameter value as a key value pair.
                Example: `lcaac $commandName <process name> -D x="12 kg" -D geo="UK" -f params.csv`.
            """.trimIndent())
    .associate()

fun CliktCommand.globalsOption(commandName: String) = option("-G", "--global")
    .help(
        """
                Override global variable as a key value pair.
                Example: `lcaac $commandName <process name> -G x="12 kg"`.
            """.trimIndent()
    ).associate()


