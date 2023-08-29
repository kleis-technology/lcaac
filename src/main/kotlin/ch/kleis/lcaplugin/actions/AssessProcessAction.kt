package ch.kleis.lcaplugin.actions

import ch.kleis.lcaplugin.core.assessment.ContributionAnalysis
import ch.kleis.lcaplugin.core.assessment.ContributionAnalysisProgram
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.core.math.basic.BasicNumber
import ch.kleis.lcaplugin.core.math.basic.BasicOperations
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.ui.toolwindow.contribution_analysis.ContributionAnalysisHugeWindow
import ch.kleis.lcaplugin.ui.toolwindow.contribution_analysis.ContributionAnalysisWindow
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory

const val DISPLAY_MAX_CELLS = 1000

class AssessProcessAction(
    private val processName: String,
    private val matchLabels: Map<String, String>,
) : AnAction(
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
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Run") {
            private var data: Pair<ContributionAnalysis, Comparator<MatrixColumnIndex<BasicNumber>>>? = null

            override fun run(indicator: ProgressIndicator) {
                val trace = traceSystemWithIndicator(indicator, file, processName, matchLabels, BasicOperations)
                val order = trace.getObservableOrder()
                val analysis = ContributionAnalysisProgram(trace.getSystemValue(), trace.getEntryPoint()).run()
                this.data = Pair(analysis, order)
            }

            override fun onSuccess() {
                this.data?.let {
                    displayInventory(project, it.first, it.second)
                }
            }

            override fun onThrowable(e: Throwable) {
                val title = "Error while assessing $processName"
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("LcaAsCode")
                    .createNotification(title, e.message ?: "unknown error", NotificationType.ERROR)
                    .notify(project)
                LOG.warn("Unable to process computation", e)
            }

            private fun displayInventory(project: Project, analysis: ContributionAnalysis, order: Comparator<MatrixColumnIndex<BasicNumber>>) {
                val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Output") ?: return
                val assessResultContent = if (analysis.getNumberOfImpactFactors() <= DISPLAY_MAX_CELLS) {
                    ContributionAnalysisWindow(analysis, order, project, processName).getContent()
                } else {
                    ContributionAnalysisHugeWindow(analysis, order, "lca.dialog.export.warning", project).getContent()
                }
                val content = ContentFactory.getInstance().createContent(assessResultContent, processName, false)
                toolWindow.contentManager.addContent(content)
                toolWindow.contentManager.setSelectedContent(content)
                toolWindow.show()
            }
        })
    }

}


