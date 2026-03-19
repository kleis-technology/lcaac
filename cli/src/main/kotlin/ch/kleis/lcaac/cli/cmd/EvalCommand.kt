package ch.kleis.lcaac.cli.cmd

import ch.kleis.lcaac.core.datasource.ConnectorFactory
import ch.kleis.lcaac.core.datasource.DefaultDataSourceOperations
import ch.kleis.lcaac.core.datasource.csv.CsvConnectorBuilder
import ch.kleis.lcaac.core.lang.evaluator.ToValue
import ch.kleis.lcaac.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaac.core.math.basic.BasicOperations
import ch.kleis.lcaac.grammar.CoreMapper
import ch.kleis.lcaac.grammar.Loader
import ch.kleis.lcaac.grammar.LoaderOption
import ch.kleis.lcaac.grammar.parser.LcaLangLexer
import ch.kleis.lcaac.grammar.parser.LcaLangParser
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.help
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import java.io.File

const val evalCommandName = "eval"

class EvalCommand : CliktCommand(name = evalCommandName, help = "Evaluate a data expression") {
    val expression: String by argument().help("Data expression")
    val configFile: File by configFileOption()
    val source: File by sourceOption()
    val globals: Map<String, String> by globalsOption(evalCommandName)

    override fun run() {
        val sourceDirectory = parseSource(source)
        val projectDirectory = configFile.parentFile
        val yamlConfig = parseLcaacConfig(configFile)

        val files = lcaFiles(sourceDirectory)
        val symbolTable = Loader(
            ops = BasicOperations,
            overriddenGlobals = dataExpressionMap(BasicOperations, globals),
        ).load(files, listOf(LoaderOption.WITH_PRELUDE))

        val factory = ConnectorFactory(
            projectDirectory.path,
            yamlConfig,
            BasicOperations,
            symbolTable,
            listOf(CsvConnectorBuilder()),
        )
        val sourceOps = DefaultDataSourceOperations(BasicOperations, yamlConfig, factory.buildConnectors())
        val dataReducer = DataExpressionReducer(symbolTable.data, symbolTable.dataSources, BasicOperations, sourceOps)

        val lexer = LcaLangLexer(CharStreams.fromString(expression))
        val tokens = CommonTokenStream(lexer)
        val parser = LcaLangParser(tokens)
        val expr = CoreMapper(BasicOperations).dataExpression(parser.dataExpression())

        val result = with(ToValue(BasicOperations)) {
            dataReducer.reduce(expr).toValue()
        }
        echo(result.toString())
    }
}
