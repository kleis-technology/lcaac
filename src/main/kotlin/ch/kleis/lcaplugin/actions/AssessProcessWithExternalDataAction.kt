package ch.kleis.lcaplugin.actions

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

class AssessProcessWithExternalDataAction(
    private val processName: String,
) : AnAction(
    "Run ...",
    "Run ...",
    AllIcons.Actions.Execute,
) {
    companion object {
        private val LOG = Logger.getInstance(AssessProcessWithExternalDataAction::class.java)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(LangDataKeys.PSI_FILE) as LcaFile? ?: return
        val collector = LcaFileCollector()
        val parser = LcaLangAbstractParser(collector.collect(file))
        try {
            val symbolTable = parser.load()
            val entryPoint = symbolTable.getTemplate(processName)!!
            TODO()
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
