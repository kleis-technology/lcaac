package ch.kleis.lcaplugin.core.lang.evaluator

import arrow.optics.Every
import ch.kleis.lcaplugin.core.lang.*
import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.expression.optics.Merge
import ch.kleis.lcaplugin.core.lang.expression.optics.productRefInProductExpression

class Evaluator(
    symbolTable: SymbolTable,
) {
    private val processTemplates = symbolTable.processTemplates
    private val lcaReducer = LcaExpressionReducer(
        symbolTable.products,
        symbolTable.substances,
        symbolTable.indicators,
        symbolTable.quantities,
        symbolTable.units,
        symbolTable.substanceCharacterizations
    )
    private val templateReducer = TemplateExpressionReducer(
        symbolTable.products,
        symbolTable.substances,
        symbolTable.indicators,
        symbolTable.quantities,
        symbolTable.units,
        processTemplates,
    )

    fun eval(expression: TemplateExpression): Value {
        val reduced = step(expression)
        return asValue(reduced)
    }

    private fun completeProducts(reduced: TemplateExpression): TemplateExpression {
        return Merge(
            listOf(
                TemplateExpression.eProcessFinal.expression.eProcess.products compose Every.list(),
                TemplateExpression.eProcessFinal.expression.eProcess.inputs compose Every.list(),
            )
        ).modify(reduced) { exchange ->
            val q = exchange.quantity
            if (q !is EQuantityLiteral) {
                throw EvaluatorException("quantity $q is not reduced")
            }
            (ETechnoExchange.product compose productRefInProductExpression)
                .modify(exchange) {
                    EProduct(it.name, q.unit)
                }
        }
    }

    fun step(expression: TemplateExpression): TemplateExpression {
        val reduced = when (expression) {
            is EInstance -> templateReducer.reduce(expression)
            is EProcessFinal -> expression
            is EProcessTemplate -> templateReducer.reduce(EInstance(expression, emptyMap()))
            is ETemplateRef -> processTemplates[expression.name]?.let {
                templateReducer.reduce(EInstance(expression, emptyMap()))
            } ?: expression
        }
        val unboundedReferences = Helper().allUnboundedReferencesButProductRefs(reduced)
        if (unboundedReferences.isNotEmpty()) {
            throw EvaluatorException("unbounded references: $unboundedReferences")
        }
        return completeProducts(reduced)
    }

    fun eval(expression: LcaSubstanceCharacterizationExpression): SubstanceCharacterizationValue {
        return asValue(step(expression))
    }

    private fun step(expression: LcaSubstanceCharacterizationExpression): LcaSubstanceCharacterizationExpression {
        val reduced = lcaReducer.reduceSubstanceCharacterization(expression)
        val unboundedReferences = Helper().allUnboundedReferencesButProductRefs(reduced)
        if (unboundedReferences.isNotEmpty()) {
            throw EvaluatorException("unbounded references: $unboundedReferences")
        }
        return reduced
    }

    fun asValue(reduced: TemplateExpression): Value {
        return when (reduced) {
            is EProcessFinal -> asValue(reduced.expression)
            else -> throw EvaluatorException("$reduced is not reduced")
        }
    }

    private fun asValue(process: LcaProcessExpression): Value {
        if (process !is EProcess) {
            throw EvaluatorException("$process is not reduced")
        }
        return ProcessValue(
            process.products.map { asValue(it) },
            process.inputs.map { asValue(it) },
            process.biosphere.map { asValue(it) },
        )
    }

    private fun asValue(exchange: ETechnoExchange): TechnoExchangeValue {
        return TechnoExchangeValue(
            asValue(exchange.quantity),
            asValue(exchange.product),
        )
    }

    private fun asValue(exchange: EBioExchange): BioExchangeValue {
        return BioExchangeValue(
            asValue(exchange.quantity),
            asValue(exchange.substance),
        )
    }

    private fun asValue(quantity: QuantityExpression): QuantityValue {
        if (quantity !is EQuantityLiteral) {
            throw EvaluatorException("$quantity is not reduced")
        }
        return QuantityValue(
            quantity.amount,
            asValue(quantity.unit),
        )
    }

    private fun asValue(product: LcaProductExpression): ProductValue {
        return when (product) {
            is EConstrainedProduct -> {
                val actualProduct = product.product
                if (actualProduct !is EProduct) {
                    throw EvaluatorException("$actualProduct is not reduced")
                }
                ProductValue(
                    actualProduct.name,
                    asValue(actualProduct.referenceUnit),
                )
            }

            is EProduct -> ProductValue(
                product.name,
                asValue(product.referenceUnit),
            )

            is EProductRef -> throw EvaluatorException("$product is not reduced")
        }
    }

    private fun asValue(substance: LcaSubstanceExpression): SubstanceValue {
        return when (substance) {
            is ESubstance -> SubstanceValue(
                substance.name,
                substance.compartment,
                substance.subcompartment,
                asValue(substance.referenceUnit),
            )

            is ESubstanceRef -> throw EvaluatorException("$substance is not reduced")
        }
    }

    private fun asValue(unit: UnitExpression): UnitValue {
        return when (unit) {
            is EUnitLiteral -> UnitValue(
                unit.symbol,
                unit.scale,
                unit.dimension,
            )

            else -> throw EvaluatorException("$unit is not reduced")
        }
    }

    private fun asValue(substanceCharacterization: LcaSubstanceCharacterizationExpression): SubstanceCharacterizationValue {
        return when(substanceCharacterization) {
            is ESubstanceCharacterization -> SubstanceCharacterizationValue(
                referenceExchange = asValue(substanceCharacterization.referenceExchange),
                impacts = substanceCharacterization.impacts.map { asValue(it) },
            )
            is ESubstanceCharacterizationRef -> throw EvaluatorException("$substanceCharacterization is not reduced")
        }
    }

    private fun asValue(impact: EImpact): ImpactValue {
        return ImpactValue(
            asValue(impact.quantity),
            asValue(impact.indicator),
        )
    }

    private fun asValue(indicator: LcaIndicatorExpression): IndicatorValue {
        return when(indicator) {
            is EIndicator -> IndicatorValue(
                indicator.name,
                asValue(indicator.referenceUnit),
            )
            is EIndicatorRef -> throw EvaluatorException("$indicator is not reduced")
        }
    }

}
