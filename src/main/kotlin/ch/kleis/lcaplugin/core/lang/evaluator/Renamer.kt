package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.expression.*

class Renamer(
    private val selector: (RefExpression) -> Boolean,
) {
    fun rename(substitutions: List<Pair<String, String>>, expression: Expression): Expression {
        var result = expression
        substitutions.forEach {
            result = rename(it.first, it.second, result)
        }
        return result
    }

    private fun rename(existing: String, replacement: String, expression: Expression): Expression {
        return when (expression) {
            is EIndicatorRef ->
                if (selector(expression) && expression.name == existing)
                    EIndicatorRef(replacement)
                else expression

            is EProductRef ->
                if (selector(expression) && expression.name == existing)
                    EProductRef(replacement)
                else expression

            is ESubstanceCharacterizationRef ->
                if (selector(expression) && expression.name == existing)
                    ESubstanceCharacterizationRef(replacement)
                else expression

            is ESubstanceRef ->
                if (selector(expression) && expression.name == existing)
                    ESubstanceRef(replacement)
                else expression

            is EQuantityRef ->
                if (selector(expression) && expression.name == existing)
                    EQuantityRef(replacement)
                else expression

            is ETemplateRef ->
                if (selector(expression) && expression.name == existing)
                    ETemplateRef(replacement)
                else expression

            is EUnitRef ->
                if (selector(expression) && expression.name == existing)
                    EUnitRef(replacement)
                else expression

            is EBioExchange -> EBioExchange(
                rename(existing, replacement, expression.quantity) as QuantityExpression,
                rename(existing, replacement, expression.substance) as LcaSubstanceExpression,
            )

            is EImpact -> EImpact(
                rename(existing, replacement, expression.quantity) as QuantityExpression,
                rename(existing, replacement, expression.indicator) as LcaIndicatorExpression,
            )

            is ETechnoExchange -> ETechnoExchange(
                rename(existing, replacement, expression.quantity) as QuantityExpression,
                rename(existing, replacement, expression.product) as LcaProductExpression,
            )

            is EIndicator -> EIndicator(
                expression.name,
                rename(existing, replacement, expression.referenceUnit) as UnitExpression,
            )

            is EProcess -> EProcess(
                expression.products.map { rename(existing, replacement, it) as ETechnoExchange },
                expression.inputs.map { rename(existing, replacement, it) as ETechnoExchange },
                expression.biosphere.map { rename(existing, replacement, it) as EBioExchange },
            )

            is EConstrainedProduct -> EConstrainedProduct(
                rename(existing, replacement, expression.product) as LcaUnconstrainedProductExpression,
                expression.constraint.rename(existing, replacement),
            )

            is EProduct -> EProduct(
                expression.name,
                rename(existing, replacement, expression.referenceUnit) as UnitExpression,
            )

            is ESubstanceCharacterization -> ESubstanceCharacterization(
                rename(existing, replacement, expression.referenceExchange) as EBioExchange,
                expression.impacts.map { rename(existing, replacement, it) as EImpact },
            )

            is ESubstance -> ESubstance(
                expression.name,
                expression.compartment,
                expression.subcompartment,
                rename(existing, replacement, expression.referenceUnit) as UnitExpression,
            )

            is ESystem -> ESystem(
                expression.processes.map { rename(existing, replacement, it) as EProcess }
            )

            is EQuantityAdd -> EQuantityAdd(
                rename(existing, replacement, expression.left) as QuantityExpression,
                rename(existing, replacement, expression.right) as QuantityExpression,
            )

            is EQuantityDiv -> EQuantityDiv(
                rename(existing, replacement, expression.left) as QuantityExpression,
                rename(existing, replacement, expression.right) as QuantityExpression,
            )

            is EQuantityLiteral -> EQuantityLiteral(
                expression.amount,
                rename(existing, replacement, expression.unit) as UnitExpression,
            )

            is EQuantityMul -> EQuantityMul(
                rename(existing, replacement, expression.left) as QuantityExpression,
                rename(existing, replacement, expression.right) as QuantityExpression,
            )

            is EQuantityNeg -> EQuantityNeg(
                rename(existing, replacement, expression.quantity) as QuantityExpression
            )

            is EQuantityPow -> EQuantityPow(
                rename(existing, replacement, expression.quantity) as QuantityExpression,
                expression.exponent,
            )

            is EQuantitySub -> EQuantitySub(
                rename(existing, replacement, expression.left) as QuantityExpression,
                rename(existing, replacement, expression.right) as QuantityExpression,
            )

            is EInstance -> EInstance(
                rename(existing, replacement, expression.template) as TemplateExpression,
                expression.arguments.mapValues { rename(existing, replacement, it.value) as QuantityExpression },
            )

            is EProcessFinal -> EProcessFinal(
                rename(existing, replacement, expression.expression) as LcaProcessExpression
            )

            is EProcessTemplate -> EProcessTemplate(
                expression.params.mapValues { rename(existing, replacement, it.value) as QuantityExpression },
                expression.locals.mapValues { rename(existing, replacement, it.value) as QuantityExpression },
                rename(existing, replacement, expression.body) as LcaProcessExpression,
            )

            is EUnitDiv -> EUnitDiv(
                rename(existing, replacement, expression.left) as UnitExpression,
                rename(existing, replacement, expression.right) as UnitExpression,
            )

            is EUnitLiteral -> expression
            is EUnitMul -> EUnitMul(
                rename(existing, replacement, expression.left) as UnitExpression,
                rename(existing, replacement, expression.right) as UnitExpression,
            )

            is EUnitPow -> EUnitPow(
                rename(existing, replacement, expression.unit) as UnitExpression,
                expression.exponent,
            )
        }
    }
}
