package ch.kleis.lcaplugin.actions

import ch.kleis.lcaplugin.core.lang.evaluator.Evaluator
import ch.kleis.lcaplugin.core.lang.value.SystemValue
import ch.kleis.lcaplugin.language.parser.LcaFileCollector
import ch.kleis.lcaplugin.language.parser.LcaLangAbstractParser
import ch.kleis.lcaplugin.language.psi.LcaFile
import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.progress.ProgressIndicator

fun evaluateSystemWithIndicator(indicator: ProgressIndicator, file: LcaFile, processName: String): SystemValue {
    indicator.isIndeterminate = true

    // read
    indicator.text = "Loading symbol table"
    val symbolTable = runReadAction {
        val collector = LcaFileCollector()
        val parser = LcaLangAbstractParser(collector.collect(file))
        parser.load()
    }

    // compute
    indicator.text = "Solving system"
    val entryPoint =
        symbolTable.getTemplate(processName)!! // We are called from a process, so it must exist

    return Evaluator(symbolTable).eval(entryPoint)
}