package ch.kleis.lcaplugin.core.lang.evaluator

import ch.kleis.lcaplugin.core.lang.expression.*
import ch.kleis.lcaplugin.core.lang.value.*

fun ProcessTemplateExpression.toValue(): ProcessValue {
    return when (this) {
        is EProcessFinal -> this.expression.toValue()
        else -> throw EvaluatorException("$this is not final")
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
        this.quantity.toValue() as QuantityValue,
        this.indicator.toValue(),
    )
}

private fun EIndicatorSpec.toValue(): IndicatorValue {
    val referenceUnit = (this.referenceUnit as QuantityExpression?)
        ?.toUnitValue()
        ?: throw EvaluatorException("$this has no reference unit")
    return IndicatorValue(
        this.name,
        referenceUnit,
    )
}

fun EProcess.toValue(): ProcessValue {
    return ProcessValue(
        this.name,
        this.labels.mapValues { it.value.toValue() as StringValue },
        this.products.map { it.toValue() },
        this.inputs.map { it.toValue() },
        this.biosphere.map { it.toValue() },
    )
}

fun EBioExchange.toValue(): BioExchangeValue {
    return BioExchangeValue(
        this.quantity.toValue() as QuantityValue,
        this.substance.toValue(),
    )
}

fun ETechnoExchange.toValue(): TechnoExchangeValue {
    return TechnoExchangeValue(
        this.quantity.toValue() as QuantityValue,
        this.product.toValue(),
        this.allocation.toValue() as QuantityValue,
    )
}

fun ESubstanceSpec.toValue(): SubstanceValue {
    val referenceUnit = (this.referenceUnit as QuantityExpression?)
        ?.toUnitValue()
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
    val referenceUnitValue = (this.referenceUnit as QuantityExpression?)
        ?.toUnitValue()
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
        this.matchLabels.elements.mapValues { it.value.toValue() as StringValue },
        this.arguments.mapValues {
            when (val e = it.value) {
                is QuantityExpression -> e.toValue()
                is StringExpression -> e.toValue()
                is EDataRef -> throw EvaluatorException("$it is not reduced")
            }
        },
    )
}

fun DataExpression.toValue(): DataValue {
    return when(this) {
        is EStringLiteral -> StringValue(this.value)
        is EUnitLiteral -> QuantityValue(1.0, this.toUnitValue())
        is EQuantityScale -> when (val b = this.base) {
            is EUnitLiteral -> QuantityValue(
                this.scale, b.toUnitValue(),
            )
            else -> throw EvaluatorException("$b is not reduced")
        }

        else -> throw EvaluatorException("$this is not reduced")
    }
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

