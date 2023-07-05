package ch.kleis.lcaplugin.actions.sankey

import ch.kleis.lcaplugin.MyBundle
import ch.kleis.lcaplugin.actions.traceSystemWithIndicator
import ch.kleis.lcaplugin.core.assessment.Assessment
import ch.kleis.lcaplugin.core.graph.Graph
import ch.kleis.lcaplugin.core.lang.value.MatrixColumnIndex
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.ui.toolwindow.SankeyGraphResult
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
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import javax.swing.JTextArea
import kotlin.math.min

class SankeyGraphAction(
    private val processName: String,
    private val matchLabels: Map<String, String>,
) : AnAction(
    "Generate Graph",
    "Generate graph",
    AllIcons.Graph.Layout,
) {
    companion object {
        private val LOG = Logger.getInstance(SankeyGraphAction::class.java)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(LangDataKeys.PSI_FILE) as LcaFile? ?: return

        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Generate sankey graph") {
            private var indicatorList: List<MatrixColumnIndex>? = null
            private var graphBuilder: SankeyGraphBuilder? = null
            private var graph: Graph? = null

            override fun run(progress: ProgressIndicator) {
                val trace = traceSystemWithIndicator(progress, file, processName, matchLabels)
                val assessment = Assessment(trace.getSystemValue(), trace.getEntryPoint())
                val inventory = assessment.inventory()
                val allocatedSystem = assessment.allocatedSystem
                indicatorList = inventory.getControllablePorts().getElements()

                // FIXME: let the user choose
                val sankeyIndicator = indicatorList!!.first()

                // generate graph
                progress.text = "Generating sankey graph"
                graphBuilder = SankeyGraphBuilder(allocatedSystem, inventory)
                this.graph = graphBuilder!!.buildContributionGraph(sankeyIndicator)
            }

            override fun onSuccess() {
                graph?.let {
                    val content = buildContent(processName, it)
                    fillAndShowToolWindow(project, content)
                }
            }

            override fun onThrowable(error: Throwable) {
                val content = buildErrorContent(processName, error)
                fillAndShowToolWindow(project, content)
            }

            @Suppress("DialogTitleCapitalization")
            private fun buildErrorContent(processName: String, error: Throwable): Content {
                val title = MyBundle.message("lca.dialog.graph.error", processName)
                val msg = error.message ?: "An unknown error has occurred."
                NotificationGroupManager.getInstance()
                    .getNotificationGroup("LcaAsCode")
                    .createNotification(title, msg, NotificationType.WARNING)
                    .notify(ProjectManager.getInstance().openProjects.firstOrNull())
                LOG.warn(title, error)
                return ContentFactory.getInstance().createContent(
                    JTextArea(msg.substring(0, min(msg.length, 400))), title, false
                )
            }

            private fun buildContent(processName: String, graph: Graph): Content =
                ContentFactory.getInstance().createContent(
                    SankeyGraphResult(graph, indicatorList!!, graphBuilder!!).getContent(), "Contribution analysis of $processName", false
                )

            private fun fillAndShowToolWindow(project: Project, content: Content) {
                val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Output") ?: return
                toolWindow.contentManager.addContent(content)
                toolWindow.contentManager.setSelectedContent(content)
                toolWindow.show()
            }
        })
    }
}
