package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.*
import ch.kleis.lcaplugin.core.lang.expression.*

fun TemplateExpression.toValue(): ProcessValue {
    return when (this) {
        is EProcessFinal -> this.expression.toValue()
        else -> throw EvaluatorException("$this is not this")
    }
}

fun LcaSubstanceCharacterizationExpression.toValue(): SubstanceCharacterizationValue {
    return when (this) {
        is ESubstanceCharacterization -> SubstanceCharacterizationValue(
            referenceExchange = this.referenceExchange.toValue(),
            impacts = this.impacts.map { it.toValue() },
        )

        is ESubstanceCharacterizationRef -> throw EvaluatorException("$this is not reduced")
    }
}

fun EImpact.toValue() : ImpactValue {
    return ImpactValue(
        this.quantity.toValue(),
        this.indicator.toValue(),
    )
}

private fun LcaIndicatorExpression.toValue(): IndicatorValue {
    return when (this) {
        is EIndicator -> IndicatorValue(
            this.name,
            this.referenceUnit.toValue(),
        )

        is EIndicatorRef -> throw EvaluatorException("$this is not reduced")
    }
}

fun LcaProcessExpression.toValue(): ProcessValue {
    if (this !is EProcess) {
        throw EvaluatorException("$this is not reduced")
    }
    return ProcessValue(
        this.products.map { it.toValue() },
        this.inputs.map { it.toValue() },
        this.biosphere.map { it.toValue() },
    )
}

fun EBioExchange.toValue(): BioExchangeValue {
    return BioExchangeValue(
        this.quantity.toValue(),
        this.substance.toValue(),
    )
}

fun ETechnoExchange.toValue(): TechnoExchangeValue {
    return TechnoExchangeValue(
        this.quantity.toValue(),
        this.product.toValue(),
    )
}

fun LcaSubstanceExpression.toValue(): SubstanceValue {
    return when (this) {
        is ESubstance -> SubstanceValue(
            this.name,
            this.compartment,
            this.subcompartment,
            this.referenceUnit.toValue(),
        )

        is ESubstanceRef -> throw EvaluatorException("$this is not reduced")
    }
}

fun LcaProductExpression.toValue(): ProductValue {
    return when (this) {
        is EConstrainedProduct -> {
            val actualProduct = this.product
            if (actualProduct !is EProduct) {
                throw EvaluatorException("$actualProduct is not reduced")
            }
            ProductValue(
                actualProduct.name,
                actualProduct.referenceUnit.toValue(),
            )
        }

        is EProduct -> ProductValue(
            this.name,
            this.referenceUnit.toValue(),
        )

        is EProductRef -> throw EvaluatorException("$this is not reduced")
    }
}

fun QuantityExpression.toValue(): QuantityValue {
    if (this !is EQuantityLiteral) {
        throw EvaluatorException("$this is not reduced")
    }
    return QuantityValue(
        this.amount,
        this.unit.toValue(),
    )
}

fun UnitExpression.toValue(): UnitValue {
    return when (this) {
        is EUnitLiteral -> UnitValue(
            this.symbol,
            this.scale,
            this.dimension,
        )

        else -> throw EvaluatorException("$this is not reduced")
    }
}

