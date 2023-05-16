package ch.kleis.lcaplugin.actions

import ch.kleis.lcaplugin.actions.csv.CsvProcessor
import ch.kleis.lcaplugin.actions.csv.CsvRequestReader
import ch.kleis.lcaplugin.actions.csv.CsvResultWriter
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.matrix.InventoryError
import ch.kleis.lcaplugin.language.parser.LcaFileCollector
import ch.kleis.lcaplugin.language.parser.LcaLangAbstractParser
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.diagnostic.Logger
import kotlin.io.path.Path

class AssessProcessWithDataAction(
    private val processName: String,
) : AnAction(
    "Run with ${processName}.csv",
    "Run with ${processName}.csv",
    AllIcons.Actions.Execute,
) {
    companion object {
        private val LOG = Logger.getInstance(AssessProcessWithDataAction::class.java)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(LangDataKeys.PSI_FILE) as LcaFile? ?: return
        val containingDirectory = file.containingDirectory ?: return


        // TODO: stream "read, process, write", progress indicator bar, run in background
        try {
            // read
            val csvFile = Path(containingDirectory.virtualFile.path, "$processName.csv").toFile()
            val requestReader = CsvRequestReader(processName, csvFile.inputStream())
            val requests = requestReader.read()

            // process
            val collector = LcaFileCollector()
            val parser = LcaLangAbstractParser(collector.collect(file))
            val symbolTable = parser.load()
            val csvProcessor = CsvProcessor(symbolTable)
            val results = requests.map { request ->
                csvProcessor.process(request)
            }

            // write
            val csvResultFile = Path(containingDirectory.virtualFile.path, "$processName.results.csv").toFile()
            val resultWriter = CsvResultWriter(csvResultFile.outputStream())
            resultWriter.write(results)
            resultWriter.flush()

            // TODO: Notify user on success
        } catch (e: EvaluatorException) {
            val result = InventoryError(e.message ?: "evaluator: unknown error")
            DisplayInventoryResult(project, result).show()
            LOG.warn("Unable to process computation", e)
        } catch (e: NoSuchElementException) {
            val result = InventoryError(e.message ?: "evaluator: unknown error")
            DisplayInventoryResult(project, result).show()
            LOG.warn("Unable to process computation", e)
        }
    }
}
