package ch.kleis.lcaplugin.actions

import ch.kleis.lcaplugin.core.*
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.Evaluator
import ch.kleis.lcaplugin.core.lang.value.*
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
import org.jetbrains.annotations.Contract
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
     * Inherited method that will be called by the framework when
     */
    override fun navigate(e: MouseEvent?, mElement: PsiElement?) {
        mElement?.let{ element ->
            val processName = getProcessName(element.parent as PsiProcess)

            val content = try {
                buildContent(processName, buildSystemProcessGraph(buildSystem(processName, element)))
            } catch (e: Exception) {
                when (e.message) {
                    null ->
                        buildErrorContent(processName, "An unknown error has occurred.")
                    else ->
                        buildErrorContent(processName, e.message!!)
                }

            }
            fillAndShowToolWindow(element.project, content)
        }
    }

    private fun buildSymbolTable(file: LcaFile): SymbolTable =
            LcaLangAbstractParser(LcaFileCollector().collect(file)).load()

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
                    JTextArea(error),
                    "Error loading graph for process $processName",
                    false
            )

    /**
     * Format the data in Content form for consumption by the IntelliJ ToolWindow API.
     */
    private fun buildContent(processName: String, graph: ProcessGraph): Content =
            ContentFactory.getInstance().createContent(
                    LcaGraphChildProcessesResult(graph).getContent(),
                    "ProcessGraph for process $processName",
                    false)

    /**
     * Obtain a handle on the project ToolWindow, set our content and open it.
     * Will return with no action if the handle cannot be obtained.
     */
    private fun fillAndShowToolWindow(project: Project, content: Content) {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Output") ?: return
        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.setSelectedContent(content)
        toolWindow.show()
    }

    /** Get a name from a PsiElement known to be a PsiProcess */
    @Contract(pure = true)
    private fun getProcessName(elt: PsiProcess): String =
            elt.getProcessTemplateRef().getUID().name

    /* TODO: This is written under the understanding that a system built using an Evaluator and an entry point contains
     * TODO: completely and exclusively the processes related to that entry point. Check that assumption.
     */
    @Throws(AssertionError::class)
    private fun buildSystemProcessGraph(systemValue: SystemValue): ProcessGraph {
        fun processKey(process: ProcessValue): String = "PROCESS_${process.name}"
        fun substanceKey(exchange: BioExchangeValue): String = "SUBSTANCE_${exchange.substance.name}"
        fun productKey(exchange: TechnoExchangeValue): String = "PRODUCT_${exchange.product.name}"
        fun exchangeProcessGraphNode(exchange: ExchangeValue): ProcessGraphNode = when (exchange) {
            is BioExchangeValue -> ProcessGraphNode(
                    substanceKey(exchange),
                    ProcessGraphNodeType.SUBSTANCE,
                    exchange.substance.name)
            is TechnoExchangeValue -> ProcessGraphNode(
                    productKey(exchange),
                    ProcessGraphNodeType.PRODUCT,
                    exchange.product.name
            )
            else -> throw AssertionError("The impossible happened.")
        }
        fun exchangeProcessGraphLink(isInput: Boolean, processKey: String, exchange: ExchangeValue): ProcessGraphLink = when (exchange) {
            is BioExchangeValue -> ProcessGraphLink(
                    processKey,
                    substanceKey(exchange),
                    ProcessGraphLinkType.BIOSPHERE_EXCHANGE,
                    exchange.quantity.toString()
            )
            is TechnoExchangeValue -> ProcessGraphLink(
                    if (isInput) productKey(exchange) else processKey,
                    if (isInput) processKey else productKey(exchange),
                    ProcessGraphLinkType.TECHNOSPHERE_EXCHANGE,
                    exchange.quantity.toString()
            )
            else -> throw AssertionError("The impossible happened.")
        }
        fun exchangeProcessGraph(isInput: Boolean, processKey: String, graph: ProcessGraph, exchange: ExchangeValue): ProcessGraph =
                graph.addNode(exchangeProcessGraphNode(exchange)).addLink(exchangeProcessGraphLink(isInput, processKey, exchange))

        return systemValue.processes.fold(ProcessGraph.empty()) { graph, processValue ->
           graph.addNode(
                   ProcessGraphNode(processKey(processValue),
                           ProcessGraphNodeType.PROCESS,
                           processValue.name)
           ).merge(processValue.products.fold(ProcessGraph.empty()) { g, xchange -> exchangeProcessGraph(false, processKey(processValue), g, xchange) },
                   processValue.inputs.fold(ProcessGraph.empty()) { g, xchange -> exchangeProcessGraph(true, processKey(processValue), g, xchange) },
                   processValue.biosphere.fold((ProcessGraph.empty())) { g, xchange -> exchangeProcessGraph(false, processKey(processValue), g, xchange) })
        }
    }
}