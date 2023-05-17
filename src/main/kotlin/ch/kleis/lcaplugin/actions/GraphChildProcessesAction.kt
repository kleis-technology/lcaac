package ch.kleis.lcaplugin.actions

import ch.kleis.lcaplugin.MyBundle
import ch.kleis.lcaplugin.core.graph.Graph
import ch.kleis.lcaplugin.core.graph.GraphLink
import ch.kleis.lcaplugin.core.graph.GraphNode
import ch.kleis.lcaplugin.core.lang.evaluator.Evaluator
import ch.kleis.lcaplugin.core.lang.value.SystemValue
import ch.kleis.lcaplugin.language.parser.LcaFileCollector
import ch.kleis.lcaplugin.language.parser.LcaLangAbstractParser
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.ui.toolwindow.LcaGraphChildProcessesResult
import com.intellij.icons.AllIcons
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.application.runReadAction
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

/**
 * Callback handler for when the graph gutter icon is clicked.
 *
 * Note: Only one Run Line Action marker is taken into account in the plugin.xml, so we have to rely on the Line Marker
 * interface, which makes some namings a bit weird, as it is expected to jump around in code rather than run stuff.
 */
class GraphChildProcessesAction(private val processName: String) : AnAction(
    "Generate graph",
    "Generate graph",
    AllIcons.Graph.Layout,
) {
    companion object {
        private val LOG = Logger.getInstance(GraphChildProcessesAction::class.java)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val file = e.getData(LangDataKeys.PSI_FILE) as LcaFile? ?: return
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Generate graph") {
            private var graph: Graph? = null

            override fun run(indicator: ProgressIndicator) {
                // read
                indicator.fraction = 0.0
                indicator.text = "Loading symbol table"
                val symbolTable = runReadAction {
                    val collector = LcaFileCollector()
                    val parser = LcaLangAbstractParser(collector.collect(file))
                    parser.load()
                }

                // compute
                indicator.fraction = 0.33
                indicator.text = "Solving system"
                val entryPoint =
                    symbolTable.getTemplate(processName)!! // We are called from a process, so it must exist
                val systemValue = Evaluator(symbolTable).eval(entryPoint)

                // generate graph
                indicator.fraction = 0.66
                indicator.text = "Generating graph"
                this.graph = buildSystemProcessGraph(systemValue)
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

            /**
             * Format an error in Content form for consumption by the IntelliJ ToolWindow API.
             */
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

            /**
             * Format the data in Content form for consumption by the IntelliJ ToolWindow API.
             */
            private fun buildContent(processName: String, graph: Graph): Content =
                ContentFactory.getInstance().createContent(
                    LcaGraphChildProcessesResult(graph).getContent(), "ProcessGraph for process $processName", false
                )

            private fun fillAndShowToolWindow(project: Project, content: Content) {
                val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Output") ?: return
                toolWindow.contentManager.addContent(content)
                toolWindow.contentManager.setSelectedContent(content)
                toolWindow.show()
            }

            /* This is written under the understanding that a system built using an Evaluator and an entry point contains
             * completely and exclusively the processes related to that entry point.
             */
            private fun buildSystemProcessGraph(systemValue: SystemValue): Graph {
                return systemValue.processes.fold(Graph.empty()) { graph, processValue ->
                    val processKey = "PROCESS_${processValue.name}"
                    val processNode = GraphNode(processValue.name)

                    val productsGraph = processValue.products.fold(Graph.empty()) { g, xchange ->
                        g.addNode(GraphNode(xchange)).addLink(GraphLink(false, processKey, xchange))
                    }

                    val inputsGraph = processValue.inputs.fold(Graph.empty()) { g, xchange ->
                        g.addNode(GraphNode(xchange)).addLink(GraphLink(true, processKey, xchange))
                    }

                    val biosphereGraph = processValue.biosphere.fold((Graph.empty())) { g, xchange ->
                        g.addNode(GraphNode(xchange)).addLink(GraphLink(processKey, xchange))
                    }

                    graph.addNode(processNode).merge(productsGraph, inputsGraph, biosphereGraph)
                }
            }
        })
    }


}
