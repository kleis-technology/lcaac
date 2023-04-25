package ch.kleis.lcaplugin.actions

import ch.kleis.lcaplugin.core.graph.Graph
import ch.kleis.lcaplugin.core.graph.GraphLink
import ch.kleis.lcaplugin.core.graph.GraphNode
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.Evaluator
import ch.kleis.lcaplugin.core.lang.value.SystemValue
import ch.kleis.lcaplugin.language.parser.LcaFileCollector
import ch.kleis.lcaplugin.language.parser.LcaLangAbstractParser
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.type.PsiProcess
import ch.kleis.lcaplugin.ui.toolwindow.LcaGraphChildProcessesResult
import com.intellij.codeInsight.daemon.GutterIconNavigationHandler
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiElement
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import java.awt.event.MouseEvent
import javax.swing.JTextArea

/**
 * Callback handler for when the graph gutter icon is clicked.
 *
 * Note: Only one Run Line Action marker is taken into account in the plugin.xml, so we have to rely on the Line Marker
 * interface, which makes some namings a bit weird, as it is expected to jump around in code rather than run stuff.
 */
class GraphChildProcessesAction : GutterIconNavigationHandler<PsiElement> {
    /**
     * Inherited method that will be called by the framework when button is clicked.
     */
    override fun navigate(e: MouseEvent?, mElement: PsiElement?) {
        mElement?.let { element ->
            val processName = getProcessName(element.parent as PsiProcess)

            val content = try {
                buildContent(processName, buildSystemProcessGraph(buildSystem(processName, element)))
            } catch (e: Exception) {
                when (e.message) {
                    null -> buildErrorContent(processName, "An unknown error has occurred.")
                    else -> buildErrorContent(processName, e.message!!)
                }
            }
            fillAndShowToolWindow(element.project, content)
        }
    }

    private fun buildSymbolTable(file: LcaFile): SymbolTable =
        LcaLangAbstractParser(LcaFileCollector().collect(file)).load()

    /**
     * Build a coherent process/product/substance system using the core language evaluator.
     */
    private fun buildSystem(processName: String, elt: PsiElement): SystemValue {
        val file = elt.containingFile!! as LcaFile // We are called from a file, so it must exist
        val symbolTable = buildSymbolTable(file)
        val entryPoint = symbolTable.getTemplate(processName)!! // We are called from a process, so it must exist
        return Evaluator(symbolTable).eval(entryPoint)
    }

    /**
     * Format an error in Content form for consumption by the IntelliJ ToolWindow API.
     */
    private fun buildErrorContent(processName: String, error: String): Content =
        ContentFactory.getInstance().createContent(
            JTextArea(error), "Error loading graph for process $processName", false
        )

    /**
     * Format the data in Content form for consumption by the IntelliJ ToolWindow API.
     */
    private fun buildContent(processName: String, graph: Graph): Content = ContentFactory.getInstance().createContent(
        LcaGraphChildProcessesResult(graph).getContent(), "ProcessGraph for process $processName", false
    )

    private fun fillAndShowToolWindow(project: Project, content: Content) {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Output") ?: return
        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.setSelectedContent(content)
        toolWindow.show()
    }

    private fun getProcessName(elt: PsiProcess): String = elt.getProcessTemplateRef().getUID().name

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
}