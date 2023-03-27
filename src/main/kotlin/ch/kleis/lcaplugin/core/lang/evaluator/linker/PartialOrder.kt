package ch.kleis.lcaplugin.core.lang.evaluator.linker

import arrow.core.tail
import ch.kleis.lcaplugin.core.lang.value.ConstraintValue
import ch.kleis.lcaplugin.core.lang.value.FromProcessRefValue
import ch.kleis.lcaplugin.core.lang.value.NoneValue
import ch.kleis.lcaplugin.core.lang.value.ProductValue

class ProductPartialOrder {
    private val constraintPartialOrder = ConstraintPartialOrder()

    fun moreConcreteThanOrEqualTo(a: ProductValue, b: ProductValue): Boolean {
        return a.name == b.name && a.referenceUnit == b.referenceUnit
                && constraintPartialOrder.moreConcreteThanOrEqualTo(a.constraint, b.constraint)
    }

    fun mostConcreteElementsOf(elements: List<ProductValue>): List<ProductValue> {
        if (elements.isEmpty()) {
            return emptyList()
        }
        val head = elements.first()
        val tail = this.mostConcreteElementsOf(elements.tail())
        if (tail.any { this.moreConcreteThanOrEqualTo(it, head) }) {
            return tail
        }
        return tail.filter { !this.moreConcreteThanOrEqualTo(head, it) }.plus(head)
    }
}

class ConstraintPartialOrder {
    fun moreConcreteThanOrEqualTo(a: ConstraintValue, b: ConstraintValue): Boolean {
        return when (a) {
            is FromProcessRefValue -> when (b) {
                is FromProcessRefValue -> a.name == b.name
                        && a.arguments == b.arguments

                NoneValue -> true
            }

            NoneValue -> b == NoneValue
        }
    }
}
