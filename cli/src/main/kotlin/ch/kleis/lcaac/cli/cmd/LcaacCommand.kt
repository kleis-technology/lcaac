package ch.kleis.lcaac.cli.cmd

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option

class LcaacCommand : CliktCommand(name = "lcaac") {
    private val stacktraceOption: Boolean by option("--stacktrace", help = "Print the stack trace of errors").flag()

    /** Read after parsing, when an error is reported: false if the root command did not run. */
    var stacktrace = false
        private set

    override fun run() {
        stacktrace = stacktraceOption
    }
}
