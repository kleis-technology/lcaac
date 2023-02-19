package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.*
import kotlin.math.pow

class Reducer(environment: Map<String, Expression>) {
    private val environment = HashMap(environment)
    private val beta = Beta()

    fun reduce(expression: Expression): Expression {
        return when (expression) {
            is EInstance -> {
                val arguments = expression.arguments

                val template = reduce(expression.template)
                if (template !is ETemplate) {
                    return expression
                }

                val defaultValues = template.params
                    .filterValues { it != null }
                    .mapValues { it.value!! }

                val actualArguments = defaultValues
                    .plus(arguments)
                var body = template.body
                actualArguments.forEach { binder, value ->
                    body = reduce(beta.substitute(binder, value, body))
                }

                return body
            }

            is ELet -> {
                val locals = expression.locals
                val localReducer = Reducer(environment)
                locals.forEach {
                    localReducer.environment[it.key] = localReducer.reduce(it.value)
                }
                return localReducer.reduce(expression.body)
            }

            is EVar -> {
                val name = expression.name
                return environment[name] ?: expression
            }

            is ETemplate -> {
                return expression
            }

            is EBlock -> {
                return EBlock(
                    expression.elements.map { reduce(it) },
                    expression.polarity,
                )
            }

            is EExchange -> {
                return EExchange(
                    reduce(expression.quantity),
                    reduce(expression.product),
                )
            }

            is EProcess -> {
                return EProcess(
                    expression.elements
                        .map { reduce(it) }
                        .flatMap { openProcessOrBlock(it) }
                )
            }
            is ESystem -> {
                return ESystem(
                    expression.elements
                        .map { reduce(it) }
                        .flatMap { openSystem(it) }
                )
            }

            is EProduct -> EProduct(
                expression.name,
                expression.dimension,
                reduce(expression.referenceUnit)
            )

            is EAdd -> {
                val a = reduce(expression.left)
                if (a !is EQuantity) {
                    return EAdd(a, expression.right)
                }
                val aUnit = a.unit
                if (aUnit !is EUnit) {
                    return EAdd(a, expression.right)
                }

                val b = reduce(expression.right)
                if (b !is EQuantity) {
                    return EAdd(a, b)
                }
                val bUnit = b.unit
                if (bUnit !is EUnit) {
                    return EAdd(a, b)
                }

                if (aUnit.dimension != bUnit.dimension) {
                    throw EvaluatorException("incompatible dimensions in $expression")
                }

                val resultUnit = if (aUnit.scale > bUnit.scale) aUnit else bUnit
                val resultAmount = (a.amount * aUnit.scale + b.amount * bUnit.scale) / resultUnit.scale
                return EQuantity(
                    resultAmount,
                    resultUnit,
                )
            }

            is ESub -> {
                val a = reduce(expression.left)
                if (a !is EQuantity) {
                    return ESub(a, expression.right)
                }
                val aUnit = a.unit
                if (aUnit !is EUnit) {
                    return ESub(a, expression.right)
                }

                val b = reduce(expression.right)
                if (b !is EQuantity) {
                    return ESub(a, b)
                }
                val bUnit = b.unit
                if (bUnit !is EUnit) {
                    return ESub(a, b)
                }

                if (aUnit.dimension != bUnit.dimension) {
                    throw EvaluatorException("incompatible dimensions in $expression")
                }

                val resultUnit = if (aUnit.scale > bUnit.scale) aUnit else bUnit
                val resultAmount = (a.amount * aUnit.scale - b.amount * bUnit.scale) / resultUnit.scale
                return EQuantity(
                    resultAmount,
                    resultUnit,
                )
            }

            is EDiv -> {
                val a = reduce(expression.left)
                if (a !is EQuantity) {
                    return EDiv(a, expression.right)
                }
                val aUnit = a.unit
                if (aUnit !is EUnit) {
                    return EDiv(a, expression.right)
                }

                val b = reduce(expression.right)
                if (b !is EQuantity) {
                    return EDiv(a, b)
                }
                val bUnit = b.unit
                if (bUnit !is EUnit) {
                    return EDiv(a, b)
                }

                val resultUnit = aUnit.divide(bUnit)
                val resultAmount = a.amount / b.amount
                return EQuantity(
                    resultAmount,
                    resultUnit,
                )
            }

            is EMul -> {
                val a = reduce(expression.left)
                if (a !is EQuantity) {
                    return EDiv(a, expression.right)
                }
                val aUnit = a.unit
                if (aUnit !is EUnit) {
                    return EDiv(a, expression.right)
                }

                val b = reduce(expression.right)
                if (b !is EQuantity) {
                    return EDiv(a, b)
                }
                val bUnit = b.unit
                if (bUnit !is EUnit) {
                    return EDiv(a, b)
                }

                val resultUnit = aUnit.multiply(bUnit)
                val resultAmount = a.amount * b.amount
                return EQuantity(
                    resultAmount,
                    resultUnit,
                )
            }

            is ENeg -> {
                val a = reduce(expression.quantity)
                if (a !is EQuantity) {
                    return ENeg(a)
                }
                return EQuantity(
                    -a.amount,
                    a.unit,
                )
            }

            is EPow -> {
                val a = reduce(expression.quantity)
                val exponent = expression.exponent
                if (a !is EQuantity) {
                    return EPow(a, exponent)
                }
                val aUnit = a.unit
                if (aUnit !is EUnit) {
                    return EPow(a, exponent)
                }

                return EQuantity(
                    a.amount.pow(exponent),
                    aUnit.pow(exponent),
                )
            }

            is EQuantity -> {
                return EQuantity(
                    expression.amount,
                    reduce(expression.unit),
                )
            }

            is EUnit -> {
                return EUnit(
                    expression.symbol,
                    expression.scale,
                    expression.dimension,
                )
            }
        }
    }

    private fun openProcessOrBlock(expression: Expression): List<Expression> {
        return when(expression) {
            is EBlock -> {
                val elements = expression.elements
                if (expression.polarity == Polarity.POSITIVE) {
                    return elements
                }
                return elements.map {negate(it)}
            }
            is EProcess -> {
                return expression.elements
            }
            else -> listOf(expression)
        }
    }

    private fun openSystem(expression: Expression): List<Expression> {
        return when(expression) {
            is ESystem -> {
                return expression.elements
            }
            else -> listOf(expression)
        }
    }

    private fun negate(expression: Expression): Expression {
        return when(expression) {
            is EExchange -> EExchange(
                negate(expression.quantity),
                expression.product,
            )
            is EQuantity -> EQuantity(
                -expression.amount,
                expression.unit,
            )
            else -> expression
        }
    }
}
