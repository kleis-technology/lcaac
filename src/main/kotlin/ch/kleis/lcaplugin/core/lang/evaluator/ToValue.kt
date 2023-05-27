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

private fun EIndicatorSpec.toValue(): IndicatorValue {
    val referenceUnit = this.referenceUnit?.toUnitValue()
        ?: throw EvaluatorException("$this has no reference unit")
    return IndicatorValue(
        this.name,
        referenceUnit,
    )
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

fun ESubstanceSpec.toValue(): SubstanceValue {
    val referenceUnit = this.referenceUnit?.toUnitValue()
        ?: throw EvaluatorException("$this has no reference unit")
    val type = this.type ?: return PartiallyQualifiedSubstanceValue(this.name, referenceUnit)
    val compartment = this.compartment ?: return PartiallyQualifiedSubstanceValue(this.name, referenceUnit)
    return FullyQualifiedSubstanceValue(
        this.name,
        type,
        compartment,
        this.subCompartment,
        referenceUnit,
    )
}

fun EProductSpec.toValue(): ProductValue {
    val name = this.name
    val referenceUnitValue = this.referenceUnit?.toUnitValue()
        ?: throw EvaluatorException("$this has no reference unit")
    val fromProcessRefValue = this.fromProcess?.toValue()
    return ProductValue(
        name,
        referenceUnitValue,
        fromProcessRefValue,
    )
}

private fun FromProcess.toValue(): FromProcessRefValue {
    return FromProcessRefValue(
        this.name,
        this.arguments.mapValues {
            when (val e = it.value) {
                is QuantityExpression -> e.toValue()
                is StringExpression -> e.toValue()
            }
        },
    )
}

fun StringExpression.toValue(): StringValue =
    when (this) {
        is EStringLiteral -> StringValue(this.value)
        else -> throw EvaluatorException("$this is not reduced")
    }

fun QuantityExpression.toValue(): QuantityValue =
    when {
        this is EQuantityScale && this.base is EUnitLiteral ->
            QuantityValue(
                this.scale,
                this.base.toUnitValue(),
            )

        this is EUnitLiteral ->
            QuantityValue(1.0, this.toUnitValue())

        else -> throw EvaluatorException("$this is not reduced")
    }

fun QuantityExpression.toUnitValue(): UnitValue =
    when {
        this is EQuantityScale && this.base is EUnitLiteral ->
            UnitValue(
                this.base.symbol.scale(this.scale),
                this.scale * this.base.scale,
                this.base.dimension,
            )

        this is EUnitLiteral ->
            UnitValue(
                this.symbol,
                this.scale,
                this.dimension,
            )

        else -> throw EvaluatorException("$this is not reduced")
    }

