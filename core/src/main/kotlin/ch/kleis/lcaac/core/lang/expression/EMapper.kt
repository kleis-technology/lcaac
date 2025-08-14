package ch.kleis.lcaac.core.lang.expression

import ch.kleis.lcaac.core.lang.value.*

object EMapper {
    fun <Q> toDataExpression(value: DataValue<Q>): DataExpression<Q> {
        return when (value) {
            is QuantityValue -> value.toEQuantityScale()
            is RecordValue -> value.toERecord()
            is StringValue -> value.toEStringLiteral()
        }
    }

    fun <Q> toFromProcess(value: FromProcessRefValue<Q>): FromProcess<Q> {
        val labels = MatchLabels(value.matchLabels.map { it.key to it.value.toEStringLiteral() }.toMap())
        val arguments: Map<String, DataExpression<Q>> = value.arguments.map { it.key to toDataExpression(it.value) }.toMap()
        return FromProcess(value.name, labels, arguments)
    }

    fun <Q> toETechnoExchange(quantity: QuantityValue<Q>, product: ProductValue<Q>): ETechnoExchange<Q> {
        return ETechnoExchange(
            quantity = quantity.toEQuantityScale(),
            product = EProductSpec(
                product.name,
                product.referenceUnit.toEUnitLiteral(),
                product.fromProcessRef?.let { toFromProcess(it) }
            )
        )
    }

    fun <Q> toETechnoExchange(value: TechnoExchangeValue<Q>): ETechnoExchange<Q> {
        return ETechnoExchange(
            quantity = value.quantity.toEQuantityScale(),
            product = EProductSpec(
                value.product.name,
                value.product.referenceUnit.toEUnitLiteral(),
                value.product.fromProcessRef?.let { toFromProcess(it) }
            ),
            allocation = value.allocation?.toEQuantityScale()
        )
    }

    fun <Q> toEBioExchange(quantity: QuantityValue<Q>, substance: SubstanceValue<Q>): EBioExchange<Q> {
        return EBioExchange(
            quantity = quantity.toEQuantityScale(),
            substance = when (substance) {
                is FullyQualifiedSubstanceValue -> ESubstanceSpec(
                    name = substance.getShortName(),
                    displayName = substance.getDisplayName(),
                    type = substance.type,
                    compartment = substance.compartment,
                    subCompartment = substance.subcompartment,
                    referenceUnit = substance.referenceUnit.toEUnitLiteral()
                )
                is PartiallyQualifiedSubstanceValue -> ESubstanceSpec(
                    name = substance.getShortName(),
                    displayName = substance.getDisplayName(),
                    referenceUnit = substance.referenceUnit.toEUnitLiteral()
                )
            }
        )
    }

    fun <Q> toEImpact(quantity: QuantityValue<Q>, value: IndicatorValue<Q>): EImpact<Q> {
        return EImpact(
            quantity = quantity.toEQuantityScale(),
            indicator = EIndicatorSpec(value.name, value.referenceUnit.toEUnitLiteral())
        )
    }
}