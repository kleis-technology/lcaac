package ch.kleis.lcaplugin.core.lang.evaluator.linker

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.evaluator.compiler.UnlinkedSystem
import ch.kleis.lcaplugin.core.lang.expression.ConstraintFlag
import ch.kleis.lcaplugin.core.lang.expression.EConstrainedProduct
import ch.kleis.lcaplugin.core.lang.value.FromProcessRefValue
import ch.kleis.lcaplugin.core.lang.value.NoneValue
import ch.kleis.lcaplugin.core.lang.value.ProductValue

class ProductMatcher(
    private val systemObject: UnlinkedSystem,
    private val productPartialOrder: ProductPartialOrder = ProductPartialOrder(),
) {
    private val products = systemObject.getProcesses()
        .flatMap { it.products }
        .map { it.product }
        .groupBy { it.name }

    fun match(product: ProductValue): ProductValue? {
        val group = products[product.name] ?: emptyList()
        val candidates = group
            .filter { productPartialOrder.moreConcreteThanOrEqualTo(it, product) }
            .let { productPartialOrder.mostConcreteElementsOf(it) }
        if (candidates.size > 1) {
            val defaultCandidates = candidates.filter { isDefault(it) }
            if (defaultCandidates.size > 1) {
                throw EvaluatorException("more than one default matches for ${product.name}")
            }
            return defaultCandidates.firstOrNull()
                ?: throw EvaluatorException("too many matches without default for ${product.name}")
        }
        return candidates.firstOrNull()
    }

    private fun isDefault(it: ProductValue): Boolean {
        return when (it.constraint) {
            is FromProcessRefValue -> it.constraint.flag == ConstraintFlag.IS_DEFAULT
            NoneValue -> false
        }
    }
}
