package ch.kleis.lcaplugin.actions

import ch.kleis.lcaplugin.core.assessment.Assessment
import ch.kleis.lcaplugin.core.lang.evaluator.Evaluator
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.matrix.InventoryError
import ch.kleis.lcaplugin.core.matrix.InventoryResult
import ch.kleis.lcaplugin.language.parser.LcaFileCollector
import ch.kleis.lcaplugin.language.parser.LcaLangAbstractParser
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.ui.toolwindow.LcaProcessAssessResult
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory

class AssessProcessAction(private val processName: String) : AnAction(
    "Run with default parameters",
    "Run with default parameters",
    AllIcons.Actions.Execute,
) {
    companion object {
        private val LOG = Logger.getInstance(AssessProcessAction::class.java)
    }

    override fun actionPerformed(e: AnActionEvent) {

        val project = e.project ?: return
        val file = e.getData(LangDataKeys.PSI_FILE) as LcaFile? ?: return
        val collector = LcaFileCollector()
        val parser = LcaLangAbstractParser(collector.collect(file))

        try {
            val symbolTable = parser.load()
            val entryPoint = symbolTable.getTemplate(processName)!!
            val system = Evaluator(symbolTable).eval(entryPoint)
            val assessment = Assessment(system)
            val result = assessment.inventory()
            DisplayInventoryResult(project, result).show()
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


