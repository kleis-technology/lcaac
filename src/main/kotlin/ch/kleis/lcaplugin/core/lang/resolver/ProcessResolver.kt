package ch.kleis.lcaplugin.core.lang.resolver

import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.EDataRef
import ch.kleis.lcaplugin.core.lang.expression.EProcessTemplate
import ch.kleis.lcaplugin.core.lang.expression.EProductSpec
import ch.kleis.lcaplugin.core.lang.expression.EStringLiteral

class ProcessResolver(
    private val symbolTable: SymbolTable
) {
    fun resolve(spec: EProductSpec): EProcessTemplate? {
        if (spec.fromProcess == null) {
            return symbolTable.getFirstTemplateOrNullByProductName(spec.name)
        }
        val name = spec.fromProcess.name
        val labels = spec.fromProcess.matchLabels.elements
            .mapValues {
                when (val v = it.value) {
                    is EStringLiteral -> v.value
                    else -> throw EvaluatorException("$v is not a valid label value")
                }
            }
        return symbolTable.getTemplate(name, labels)?.let { candidate ->
            val providedProducts = candidate.body.products.map { it.product.name }
            if (!providedProducts.contains(spec.name)) {
                val s = if (labels.isEmpty()) name else "$name$labels"
                throw EvaluatorException("no process '$s' providing '${spec.name}' found")
            }
            candidate
        }
    }
}
