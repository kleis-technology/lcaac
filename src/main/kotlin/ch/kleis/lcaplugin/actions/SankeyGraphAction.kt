package ch.kleis.lcaplugin.actions

import ch.kleis.lcaplugin.MyBundle
import ch.kleis.lcaplugin.core.assessment.Assessment
import ch.kleis.lcaplugin.core.assessment.Inventory
import ch.kleis.lcaplugin.core.graph.Graph
import ch.kleis.lcaplugin.core.graph.GraphLink
import ch.kleis.lcaplugin.core.graph.GraphNode
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaplugin.core.lang.value.*
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
        ProgressManager.getInstance().run(object : Task.Backgroundable(project, "Generate graph") {
            private var graph: Graph? = null

            override fun run(indicator: ProgressIndicator) {
                val trace = traceSystemWithIndicator(indicator, file, processName, matchLabels)
                val inventory = Assessment(trace.getSystemValue(), trace.getEntryPoint()).inventory()

                // generate graph
                indicator.text = "Generating graph"
                // FIXME: let the user choose !
                val sankeyPort = inventory.getControllablePorts().getElements().first()

                this.graph = buildContributionGraph(sankeyPort, trace, inventory)
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
                    SankeyGraphResult(graph).getContent(), "ProcessGraph for process $processName", false
                )

            private fun fillAndShowToolWindow(project: Project, content: Content) {
                val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Output") ?: return
                toolWindow.contentManager.addContent(content)
                toolWindow.contentManager.setSelectedContent(content)
                toolWindow.show()
            }
        })
    }

    fun buildContributionGraph(
        observed: MatrixColumnIndex,
        trace: EvaluationTrace,
        inventory: Inventory,
    ): Graph {
        val portsWithObservedImpact = inventory.getObservablePorts().getElements().filter { observable ->
            // FIXME: what delta to use ?
            inventory.supply.quantityOf(observable).amount > 10E-6 && inventory.impactFactors.valueRatio(observable, observed).amount > 10E-6
        }.toSet()

        val productToProcessMap = portsWithObservedImpact
            .filterIsInstance<ProductValue>()
            .associateWith { productValue ->
                trace.getSystemValue().processes.first { processValue: ProcessValue ->
                    processValue.products.any { tev -> tev.product == productValue }
                }
            }
        return portsWithObservedImpact.fold(
            Graph.empty().addNode(GraphNode(observed.getUID(), observed.getDisplayName()))
        ) { graph, port ->
            when (port) {
                is SubstanceValue -> {
                    graph.addNode(GraphNode(port.getUID(), port.getDisplayName()))
                        .addLink(
                            GraphLink(
                                port.getUID(),
                                observed.getUID(),
                                impactAmountForSubstance(observed, inventory, port)
                            )
                        )
                }

                is ProductValue -> {
                    val parentProcess = productToProcessMap[port]!!
                    val allocationFactor = parentProcess.products.first { it.product == port }.allocation.amount / 100

                    val linksWithObservedImpact = (parentProcess.inputs + parentProcess.biosphere).filter { parentProcessExchange ->
                        portsWithObservedImpact.contains(parentProcessExchange.port()) || parentProcessExchange.port() == observed
                    }

                    linksWithObservedImpact.fold(graph.addNode(GraphNode(port.getUID(), port.getDisplayName()))) { accumulatorGraph, exchange ->
                        accumulatorGraph.addLink(
                            GraphLink(
                                port.getUID(),
                                exchange.port().getUID(),
                                impactAmountForExchange(observed, inventory, port, allocationFactor, exchange)))
                    }
                }

                else -> graph
            }
        }
    }

    private fun impactAmountForSubstance(observed: MatrixColumnIndex, inventory: Inventory, substance: SubstanceValue): Double {
        return inventory.impactFactors.valueRatio(substance, observed).referenceValue() *
            inventory.supply.quantityOf(substance).referenceValue()
    }

    private fun impactAmountForExchange(
        observed: MatrixColumnIndex,
        inventory: Inventory,
        product: ProductValue,
        allocationFactor: Double,
        exchange: ExchangeValue
    ): Double {
        val valueRatioForObservedImpact = when {
            (exchange.port() == observed) -> 1.0
            else -> inventory.impactFactors.valueRatio(exchange.port(), observed).referenceValue()
        }

        return valueRatioForObservedImpact * allocationFactor *
            inventory.supply.quantityOf(product).referenceValue() *
            exchange.quantity().referenceValue()
    }

}
