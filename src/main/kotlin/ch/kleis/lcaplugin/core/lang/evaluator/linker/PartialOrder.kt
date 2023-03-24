package ch.kleis.lcaplugin.core.lang.evaluator.linker

import arrow.core.tail
import ch.kleis.lcaplugin.core.lang.value.ConstraintValue
import ch.kleis.lcaplugin.core.lang.value.FromProcessRefValue
import ch.kleis.lcaplugin.core.lang.value.NoneValue
import ch.kleis.lcaplugin.core.lang.value.ProductValue

interface PartialOrder<V> {
    fun leq(a: V, b: V): Boolean
    fun minimal(elements: List<V>): List<V> {
        if (elements.isEmpty()) {
            return emptyList()
        }
        val head = elements.first()
        val tail = this.minimal(elements.tail())
        if (tail.any { this.leq(it, head) }) {
            return tail
        }
        return tail.filter { !this.leq(head, it) }.plus(head)
    }
}

class ProductPartialOrder(
    private val constraintPartialOrder: PartialOrder<ConstraintValue> = ConstraintPartialOrder()
) : PartialOrder<ProductValue> {
    override fun leq(a: ProductValue, b: ProductValue): Boolean {
        return a.name == b.name && a.referenceUnit == b.referenceUnit
                && constraintPartialOrder.leq(a.constraint, b.constraint)
    }
}

class ConstraintPartialOrder : PartialOrder<ConstraintValue> {
    override fun leq(a: ConstraintValue, b: ConstraintValue): Boolean {
        return when(a) {
            is FromProcessRefValue -> when (b) {
                is FromProcessRefValue -> a.name == b.name
                        && a.arguments == b.arguments
                NoneValue -> true
            }
            NoneValue -> b == NoneValue
        }
    }
}
