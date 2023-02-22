package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.*

class Beta {
    private val helper = Helper()

    fun substitute(substitutions: List<Pair<String, Expression>>, expression: Expression): Expression {
        var result = expression
        substitutions.forEach {
            result = substitute(it.first, it.second, result)
        }
        return result
    }

    fun substitute(binder: String, value: Expression, expression: Expression): Expression {
        return when (expression) {
            is ETemplate -> {
                val params = expression.params

                // parameter shadows binder
                if (params.map { it.key }.contains(binder)) {
                    return expression
                }

                // rename parameters conflicting with free variables of value
                val p = renameParamsAndBody(value, params, expression.body)
                val renamedParams = p.first
                val renamedBody = p.second

                // propagate substitution
                return ETemplate(
                    renamedParams
                        .map { entry ->
                            Pair(
                                entry.key,
                                entry.value?.let { substitute(binder, value, it) }
                            )
                        }.toMap(),
                    substitute(binder, value, renamedBody),
                )
            }

            is ELet -> {
                val locals = expression.locals

                // parameter shadows binder
                if (locals.keys.contains(binder)) {
                    return expression
                }

                // rename parameters conflicting with free variables of value
                val p = renameParamsAndBody(value, locals, expression.body)
                val renamedLocals = p.first
                val renamedBody = p.second

                // propagate substitution
                return ELet(
                    renamedLocals
                        .map { entry ->
                            Pair(entry.key, substitute(binder, value, entry.value!!))
                        }.toMap(),
                    substitute(binder, value, renamedBody),
                )
            }

            is EVar -> {
                if (binder == expression.name) {
                    return value
                }
                return expression
            }

            is EBlock -> {
                return EBlock(
                    expression.elements.map { substitute(binder, value, it) },
                    expression.polarity
                )
            }

            is EExchange -> EExchange(
                substitute(binder, value, expression.quantity),
                substitute(binder, value, expression.product),
            )
            is EInstance -> EInstance(
                substitute(binder, value, expression.template),
                expression.arguments.mapValues { substitute(binder, value, it.value) },
            )
            is EProcess -> EProcess(
                expression.elements.map { substitute(binder, value, it) }
            )
            is EProduct -> EProduct(
                expression.name,
                substitute(binder, value, expression.referenceUnit),
            )
            is EAdd -> EAdd(
                substitute(binder, value, expression.left),
                substitute(binder, value, expression.right),
            )
            is EDiv -> EDiv(
                substitute(binder, value, expression.left),
                substitute(binder, value, expression.right),
            )
            is EMul -> EMul(
                substitute(binder, value, expression.left),
                substitute(binder, value, expression.right),
            )
            is ENeg -> ENeg(
                substitute(binder, value, expression.quantity)
            )
            is EPow -> EPow(
                substitute(binder, value, expression.quantity),
                expression.exponent,
            )
            is ESub -> ESub(
                substitute(binder, value, expression.left),
                substitute(binder, value, expression.right),
            )
            is EQuantity -> EQuantity(
                expression.amount,
                substitute(binder, value, expression.unit),
            )
            is ESystem -> ESystem(
                expression.elements.map { substitute(binder, value, it) }
            )
            is EUnit -> EUnit(
                expression.symbol,
                expression.scale,
                expression.dimension,
            )
        }
    }

    private fun renameParamsAndBody(
        value: Expression,
        params: Map<String, Expression?>,
        body: Expression
    ): Pair<Map<String, Expression?>, Expression> {
        val valueFreeVars = helper.freeVariables(value)
        val conflicts = valueFreeVars
            .intersect(params.keys)
            .associateWith { helper.newName(it, valueFreeVars) }
        val renamedParams = HashMap<String, Expression?>(params)
        var renamedBody: Expression = body
        conflicts.forEach { existing, replacement ->
            renamedParams[replacement] = renamedParams[existing]
            renamedParams.remove(existing)
            renamedBody = helper.rename(existing, replacement, renamedBody)
        }
        return Pair(renamedParams, renamedBody)
    }

}

