package ch.kleis.lcaac.cli

import ch.kleis.lcaac.cli.cmd.*
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaac.grammar.LoaderException
import ch.kleis.lcaac.grammar.SyntaxException
import com.github.ajalt.clikt.core.CliktError
import com.github.ajalt.clikt.core.subcommands
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val command = LcaacCommand()
        .subcommands(
            AssessCommand(),
            EvalCommand(),
            GraphCommand(),
            TestCommand(),
            TraceCommand(),
            VersionCommand(),
        )
    // Same as CliktCommand.main, except that errors in the user's model or arguments
    // are printed as a message instead of a stack trace, unless --stacktrace is given.
    try {
        withCleanErrors { command.parse(args) }
    } catch (e: CliktError) {
        val cause = e.cause
        if (command.stacktrace && cause != null) throw cause
        command.echoFormattedHelp(e)
        exitProcess(e.statusCode)
    }
}

/**
 * Turns the errors caused by the user's input (syntax, loading, evaluation) into [CliktError]s,
 * which Clikt prints without a stack trace. Any other exception is a bug and is left as is.
 */
fun <T> withCleanErrors(block: () -> T): T = try {
    block()
} catch (e: SyntaxException) {
    throw CliktError(e.message, e)
} catch (e: LoaderException) {
    throw CliktError(e.message, e)
} catch (e: EvaluatorException) {
    throw CliktError(e.describe(), e)
}
