package ch.kleis.lcaac.cli

import ch.kleis.lcaac.cli.cmd.*
import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) = LcaacCommand()
    .subcommands(
        AssessCommand(),
        TestCommand(),
        TraceCommand(),
        VersionCommand(),
    )
    .main(args)
