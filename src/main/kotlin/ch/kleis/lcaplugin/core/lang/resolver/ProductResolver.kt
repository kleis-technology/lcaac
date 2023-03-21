package ch.kleis.lcaplugin.core.lang.resolver

import ch.kleis.lcaplugin.core.lang.SymbolTable
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.EProduct
import ch.kleis.lcaplugin.core.lang.expression.EProductRef

class ProductResolver(
    private val symbolTable: SymbolTable
) {
    fun resolve(productName: String): EProduct? {
        return recursiveResolve(emptyList(), productName)
    }

    private fun recursiveResolve(visited: List<String>, productName: String): EProduct? {
        if (visited.contains(productName)) {
            throw EvaluatorException("cyclic references: $visited")
        }
        return when(val p = symbolTable.products[productName]) {
            is EProduct -> p
            is EProductRef -> recursiveResolve(visited.plus(productName), p.name)
            null -> null
        }
    }
}
