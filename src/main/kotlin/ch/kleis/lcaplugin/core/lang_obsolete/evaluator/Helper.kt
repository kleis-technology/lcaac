package ch.kleis.lcaplugin.core.lang_obsolete.evaluator

import ch.kleis.lcaplugin.core.lang_obsolete.*

class Helper {
    fun rename(existing: String, replacement: String, expression: Expression): Expression {
        return when (expression) {
            is EVar -> {
                val name = expression.name
                if (name == existing) {
                    return EVar(replacement)
                }
                return expression
            }

            is EBlock -> EBlock(
                expression.elements.map { rename(existing, replacement, it) },
            )

            is EExchange -> EExchange(
                rename(existing, replacement, expression.quantity),
                rename(existing, replacement, expression.product),
            )

            is EInstance -> EInstance(
                rename(existing, replacement, expression.template),
                expression.arguments.mapValues { rename(existing, replacement, it.value) }
            )

            is ELet -> ELet(
                expression.locals.mapValues { rename(existing, replacement, it.value) },
                rename(existing, replacement, expression.body),
            )

            is EProcess -> EProcess(
                expression.elements.map { rename(existing, replacement, it) },
            )

            is EProduct -> EProduct(
                expression.name,
                rename(existing, replacement, expression.referenceUnit)
            )

            is EAdd -> EAdd(
                rename(existing, replacement, expression.left),
                rename(existing, replacement, expression.right),
            )

            is EDiv -> EDiv(
                rename(existing, replacement, expression.left),
                rename(existing, replacement, expression.right),
            )

            is EMul -> EMul(
                rename(existing, replacement, expression.left),
                rename(existing, replacement, expression.right),
            )

            is ENeg -> ENeg(
                rename(existing, replacement, expression.quantity),
            )

            is EPow -> EPow(
                rename(existing, replacement, expression.quantity),
                expression.exponent
            )

            is ESub -> ESub(
                rename(existing, replacement, expression.left),
                rename(existing, replacement, expression.right),
            )

            is EQuantity -> EQuantity(
                expression.amount,
                rename(existing, replacement, expression.unit),
            )

            is ESystem -> ESystem(
                expression.elements.map { rename(existing, replacement, it) },
            )

            is ETemplate -> ETemplate(
                expression.params
                    .mapValues { entry ->
                        entry.value?.let {
                            rename(existing, replacement, it)
                        }
                    },
                rename(existing, replacement, expression.body),
            )

            is EUnit -> EUnit(
                expression.symbol,
                expression.scale,
                expression.dimension,
            )
        }
    }

    fun freeVariables(expression: Expression): Set<String> {
        return freeVariables(emptySet(), expression)
    }

    fun freeVariables(boundedVariables: Set<String>, expression: Expression): Set<String> {
        return when (expression) {
            is EVar -> {
                val name = expression.name
                if (boundedVariables.contains(name)) {
                    return emptySet()
                }
                return setOf(name)
            }

            is EBlock -> expression
                .elements
                .flatMap { freeVariables(boundedVariables, it) }
                .toSet()

            is EProcess -> expression
                .elements
                .flatMap { freeVariables(boundedVariables, it) }
                .toSet()

            is ESystem -> expression
                .elements
                .flatMap { freeVariables(boundedVariables, it) }
                .toSet()

            is EExchange -> listOf(
                expression.quantity,
                expression.product,
            ).flatMap { freeVariables(boundedVariables, it) }
                .toSet()

            is EProduct -> freeVariables(
                boundedVariables,
                expression.referenceUnit,
            )

            is EAdd -> listOf(
                expression.left,
                expression.right,
            ).flatMap { freeVariables(boundedVariables, it) }
                .toSet()

            is EDiv -> listOf(
                expression.left,
                expression.right,
            ).flatMap { freeVariables(boundedVariables, it) }
                .toSet()

            is EMul -> listOf(
                expression.left,
                expression.right,
            ).flatMap { freeVariables(boundedVariables, it) }
                .toSet()

            is ENeg -> listOf(
                expression.quantity,
            ).flatMap { freeVariables(boundedVariables, it) }
                .toSet()

            is EPow -> listOf(
                expression.quantity,
            ).flatMap { freeVariables(boundedVariables, it) }
                .toSet()

            is ESub -> listOf(
                expression.left,
                expression.right,
            ).flatMap { freeVariables(boundedVariables, it) }
                .toSet()

            is EQuantity -> freeVariables(
                boundedVariables,
                expression.unit,
            )

            is EUnit -> emptySet()
            is EInstance -> (listOf(
                expression.template,
            ) + expression.arguments.map { it.value })
                .flatMap { freeVariables(boundedVariables, it) }
                .toSet()

            is ELet -> {
                val locals = expression.locals
                val newBoundedVars = boundedVariables
                    .plus(locals.keys)
                return (locals.values.toList() + listOf(expression.body))
                    .flatMap { freeVariables(newBoundedVars, it) }
                    .toSet()
            }

            is ETemplate -> {
                val params = expression.params
                val newBoundedVars = boundedVariables
                    .plus(params.keys)
                val subExpressions =
                    listOf(expression.body) + params.map { it.value }.mapNotNull { it }
                return subExpressions
                    .flatMap { freeVariables(newBoundedVars, it) }
                    .toSet()
            }
        }
    }

    fun newName(binder: String, others: Set<String>): String {
        var i = 0
        var result = "${binder}0"
        while (others.contains(result)) {
            i += 1
            result = "$binder$i"
        }
        return result
    }

}
