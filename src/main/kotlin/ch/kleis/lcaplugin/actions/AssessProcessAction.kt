package ch.kleis.lcaplugin.actions

import ch.kleis.lcaplugin.core.assessment.Assessment
import ch.kleis.lcaplugin.core.lang.evaluator.Evaluator
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.matrix.InventoryMatrix
import ch.kleis.lcaplugin.language.parser.LcaFileCollector
import ch.kleis.lcaplugin.language.parser.LcaLangAbstractParser
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.ui.toolwindow.LcaProcessAssessResult
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory

class AssessProcessAction(private val processName: String) : AnAction(
    "Run",
    "Run",
    AllIcons.Actions.Execute,
) {
    companion object {
        private val LOG = Logger.getInstance(AssessProcessAction::class.java)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(LangDataKeys.PSI_FILE) as LcaFile? ?: return

        try {
            val symbolTable = runReadAction {
                val collector = LcaFileCollector()
                val parser = LcaLangAbstractParser(collector.collect(file))
                parser.load()
            }
            val entryPoint = symbolTable.getTemplate(processName)!!
            val system = Evaluator(symbolTable).eval(entryPoint)
            val inventory = Assessment(system).inventory()
            displayInventory(project, inventory)
        } catch (e: EvaluatorException) {
            val title = "Error while assessing $processName"
            NotificationGroupManager.getInstance()
                .getNotificationGroup("LcaAsCode")
                .createNotification(title, e.message ?: "unknown error", NotificationType.ERROR)
                .notify(project)
            LOG.warn("Unable to process computation", e)
        } catch (e: NoSuchElementException) {
            val title = "Error while assessing $processName"
            NotificationGroupManager.getInstance()
                .getNotificationGroup("LcaAsCode")
                .createNotification(title, e.message ?: "unknown error", NotificationType.ERROR)
                .notify(project)
            LOG.warn("Unable to process computation", e)
        }
    }

    private fun displayInventory(project: Project, inventory: InventoryMatrix) {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Output") ?: return
        val lcaProcessAssessResult = LcaProcessAssessResult(inventory)
        val content =
            ContentFactory.getInstance().createContent(lcaProcessAssessResult.getContent(), project.name, false)
        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.setSelectedContent(content)
        toolWindow.show()
    }
}


