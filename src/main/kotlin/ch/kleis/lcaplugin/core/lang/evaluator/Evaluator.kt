package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.*

class Evaluator(private val environment: Map<String, Expression>) {
    private val reducer = Reducer(environment)
    fun eval(expression: Expression): Value {
        val reduced = reducer.reduce(expression)
        return asValue(reduced)
    }

    private fun asValue(reduced: Expression): Value {
        return when (reduced) {
            is EUnit -> {
                return VUnit(
                    reduced.symbol,
                    reduced.scale,
                    reduced.dimension,
                )
            }

            is EProduct -> {
                return VProduct(
                    reduced.name,
                    reduced.dimension,
                    asValue(reduced.referenceUnit) as VUnit,
                )
            }

            is EProcess -> {
                return VProcess(
                    reduced.elements.map { asValue(it) as VExchange }
                )
            }

            is ESystem -> {
                return VSystem(
                    reduced.elements.map { asValue(it) as VProcess }
                )
            }

            else -> throw EvaluatorException("$reduced is not reduced")
        }
    }
}
