package ch.kleis.lcaac.cli

import ch.kleis.lcaac.cli.cmd.AssessCommand
import ch.kleis.lcaac.cli.cmd.LcaacCommand
import com.github.ajalt.clikt.core.subcommands

fun main(args: Array<String>) = LcaacCommand()
    .subcommands(AssessCommand())
    .main(args)
