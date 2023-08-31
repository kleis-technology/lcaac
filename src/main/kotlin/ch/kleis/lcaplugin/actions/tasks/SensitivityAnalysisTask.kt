package ch.kleis.lcaplugin.actions.tasks

import arrow.core.filterIsInstance
import ch.kleis.lcaplugin.core.ParameterName
import ch.kleis.lcaplugin.core.assessment.SensitivityAnalysis
import ch.kleis.lcaplugin.core.assessment.SensitivityAnalysisProgram
import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.Evaluator
import ch.kleis.lcaplugin.core.lang.evaluator.ToValue
import ch.kleis.lcaplugin.core.lang.evaluator.reducer.DataExpressionReducer
import ch.kleis.lcaplugin.core.lang.expression.DataExpression
import ch.kleis.lcaplugin.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaplugin.core.lang.expression.EQuantityScale
import ch.kleis.lcaplugin.core.lang.value.QuantityValue
import ch.kleis.lcaplugin.core.math.dual.DualNumber
import ch.kleis.lcaplugin.core.math.dual.DualOperations
import ch.kleis.lcaplugin.core.matrix.IndexedCollection
import ch.kleis.lcaplugin.core.matrix.ParameterVector
import ch.kleis.lcaplugin.language.parser.LcaFileCollector
import ch.kleis.lcaplugin.language.parser.LcaLangAbstractParser
import ch.kleis.lcaplugin.language.psi.LcaFile
import ch.kleis.lcaplugin.language.psi.stub.process.ProcessStubKeyIndex
import ch.kleis.lcaplugin.psi.LcaStringExpression
import ch.kleis.lcaplugin.ui.toolwindow.sensitivity_analysis.SensitivityAnalysisWindow
import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.ui.content.ContentFactory

class SensitivityAnalysisTask(
    project: Project,
    private val file: LcaFile,
    private val processName: String,
    private val matchLabels: Map<String, String>,
) : Task.Backgroundable(project, "Sensitivity analysis"){
    private var analysis: SensitivityAnalysis? = null

    companion object {
        private val LOG = Logger.getInstance(SensitivityAnalysisTask::class.java)
    }

    fun getAnalysis(): SensitivityAnalysis? {
        return analysis
    }

    override fun run(indicator: ProgressIndicator) {
        indicator.isIndeterminate = true

        // nb params via psi
        val nbQuantitativeParams = runReadAction {
            val fqn = "${file.getPackageName()}.$processName"
            ProcessStubKeyIndex.findProcesses(project, fqn, matchLabels).first().getParameters()
                .filter { it.value !is LcaStringExpression }
                .size
        }
        val ops = DualOperations(nbQuantitativeParams)

        // read
        indicator.text = "Loading symbol table"
        val symbolTable = runReadAction {
            val collector = LcaFileCollector(file.project)
            val parser = LcaLangAbstractParser(collector.collect(file), ops)
            parser.load()
        }

        // compute
        indicator.text = "Solving system"
        val template =
            symbolTable.getTemplate(
                processName,
                matchLabels
            )!! // We are called from a process, so it must exist
        val (arguments, parameters) = prepareArguments(ops, symbolTable, template.params)
        val entryPoint = EProcessTemplateApplication(
            template,
            arguments,
        )
        val trace = Evaluator(symbolTable, ops).trace(entryPoint)
        this.analysis = SensitivityAnalysisProgram(trace.getSystemValue(), trace.getEntryPoint(), parameters).run()
    }

    override fun onSuccess() {
        this.analysis?.let {
            displayAnalysis(project, it)
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

    private fun displayAnalysis(
        project: Project,
        analysis: SensitivityAnalysis,
    ) {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("LCA Output") ?: return
        val assessResultContent = SensitivityAnalysisWindow(analysis, project, processName).getContent()
        val content = ContentFactory.getInstance().createContent(assessResultContent, processName, false)
        toolWindow.contentManager.addContent(content)
        toolWindow.contentManager.setSelectedContent(content)
        toolWindow.show()
    }

    private fun prepareArguments(
        ops: DualOperations,
        symbolTable: SymbolTable<DualNumber>,
        params: Map<String, DataExpression<DualNumber>>
    ): Pair<Map<String, DataExpression<DualNumber>>, ParameterVector<DualNumber>> {
        val dataReducer = DataExpressionReducer(symbolTable.data, ops)
        val reduced = params.mapValues { dataReducer.reduce(it.value) }
        val quantitativeArgumentList = reduced.filterIsInstance<String, EQuantityScale<DualNumber>>()
            .toList()
            .mapIndexed { index: Int, (name, value): Pair<String, EQuantityScale<DualNumber>> ->
                with(ops) {
                    name to EQuantityScale(
                        value.scale * (pure(1.0) + basis(index)),
                        value.base,
                    )
                }
            }
        val parameters = ParameterVector(
            names = IndexedCollection(quantitativeArgumentList.map { ParameterName(it.first) }),
            data = with(ToValue(ops)) { quantitativeArgumentList.map { it.second.toValue() as QuantityValue<DualNumber> } }
        )
        val arguments = reduced.plus(quantitativeArgumentList)
        return arguments to parameters
    }
}
