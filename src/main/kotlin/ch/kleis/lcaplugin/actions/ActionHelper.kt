package ch.kleis.lcaplugin.actions

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluationTrace
import ch.kleis.lcaplugin.core.lang.evaluator.Evaluator
import ch.kleis.lcaplugin.core.lang.expression.EProcessTemplateApplication
import ch.kleis.lcaplugin.core.math.QuantityOperations
import ch.kleis.lcaplugin.language.parser.LcaFileCollector
import ch.kleis.lcaplugin.language.parser.LcaLangAbstractParser
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.ProgressIndicator

fun <Q> traceSystemWithIndicator(
    indicator: ProgressIndicator,
    file: LcaFile,
    processName: String,
    matchLabels: Map<String, String>,
    ops: QuantityOperations<Q>,
): EvaluationTrace<Q> {
    indicator.isIndeterminate = true

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
        symbolTable.getTemplate(processName, matchLabels)!! // We are called from a process, so it must exist
    val entryPoint = EProcessTemplateApplication(template = template)

    return Evaluator(symbolTable, ops).trace(entryPoint)
}
