package ch.kleis.lcaplugin.core.lang.resolver

import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.EProcessTemplate
import ch.kleis.lcaplugin.core.lang.expression.EProductSpec
import ch.kleis.lcaplugin.core.lang.expression.EStringLiteral
import ch.kleis.lcaplugin.core.lang.expression.EStringRef

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
                    is EStringRef -> throw EvaluatorException("calling process $name with label ${it.key} = ${it.value} that is not a literal")
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
