package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.value.*

fun ProcessTemplateExpression.toValue(): ProcessValue {
    return when (this) {
        is EProcessFinal -> this.expression.toValue()
        else -> throw EvaluatorException("$this is not this")
    }
}

fun ESubstanceCharacterization.toValue(): SubstanceCharacterizationValue {
    return SubstanceCharacterizationValue(
        referenceExchange = this.referenceExchange.toValue(),
        impacts = this.impacts.map { it.toValue() },
    )
}

fun EImpact.toValue(): ImpactValue {
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

fun EProcess.toValue(): ProcessValue {
    return ProcessValue(
        this.name,
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
        this.allocation.toValue()
    )
}

fun LcaSubstanceExpression.toValue(): SubstanceValue {
    return when (this) {
        is ESubstance -> SubstanceValue(
            this.name,
            this.type,
            this.compartment,
            this.subcompartment,
            this.referenceUnit.toValue(),
        )

        is ESubstanceRef -> throw EvaluatorException("$this is not reduced")
    }
}

fun EProductSpec.toValue(): ProductValue {
    val name = this.name
    val referenceUnitValue = this.referenceUnit?.toValue() ?: throw EvaluatorException("$this has no reference unit")
    val fromProcessRefValue = this.fromProcessRef?.toValue()
    return ProductValue(
        name,
        referenceUnitValue,
        fromProcessRefValue,
    )
}

private fun FromProcessRef.toValue(): FromProcessRefValue {
    return FromProcessRefValue(
        this.ref,
        this.arguments.mapValues { it.value.toValue() },
    )
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

