package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.*

class Evaluator(private val environment: Environment) {
    private val reducer = Reducer(environment)
    private val helper = Helper()

    fun eval(expression: Expression): Value {
        val reduced = reducer.reduce(expression)
        val freeVars = helper.freeVariables(reduced)
        if (freeVars.isNotEmpty()) {
            val message = "undefined variables: $freeVars"
            throw EvaluatorException(message)
        }
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

            is EExchange -> {
                return VExchange(
                    asValue(reduced.quantity) as VQuantity,
                    asValue(reduced.product) as VProduct,
                )
            }

            is EQuantity -> VQuantity(
                reduced.amount,
                asValue(reduced.unit) as VUnit,
            )

            else -> throw EvaluatorException("cannot evaluate $reduced")
        }
    }
}
