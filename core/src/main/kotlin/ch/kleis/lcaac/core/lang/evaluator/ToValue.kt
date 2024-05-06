package ch.kleis.lcaac.core.lang.evaluator

import ch.kleis.lcaac.core.lang.expression.*
import ch.kleis.lcaac.core.lang.value.*
import ch.kleis.lcaac.core.math.QuantityOperations

class ToValue<Q>(
    private val ops: QuantityOperations<Q>,
) {
    private fun doubleValueOf(q: () -> Q): Double {
        return with(ops) { q().toDouble() }
    }

    fun EProcess<Q>.toValue(): ProcessValue<Q> {
        return ProcessValue(
            this.name,
            this.labels.mapValues { it.value.toValue() as StringValue<Q> },
            this.products.map { it.toValue() },
            this.inputs.flatMap { blockToValue(it) { block -> block.toValue() } },
            this.biosphere.flatMap { blockToValue(it) { block -> block.toValue() } },
            this.impacts.flatMap { blockToValue(it) { block -> block.toValue() } },
        )
    }

    private fun <E, V> blockToValue(block: BlockExpression<E, Q>, map: (E) -> V): List<V> {
        return when(block) {
            is EBlockEntry -> listOf(map(block.entry))
            is EBlockForEach -> throw EvaluatorException("block $block is not reduced")
        }
    }

    fun DataExpression<Q>.toValue(): DataValue<Q> {
        return when (this) {
            is EStringLiteral -> StringValue(this.value)

            is EQuantityScale -> when (val b = this.base) {
                is EUnitLiteral -> QuantityValue(
                    this.scale, b.toUnitValue(),
                )

                else -> throw EvaluatorException("$b is not reduced")
            }

            else -> throw EvaluatorException("$this is not reduced")
        }
    }

    fun ETechnoExchange<Q>.toValue(): TechnoExchangeValue<Q> {
        return TechnoExchangeValue(
            this.quantity.toValue() as QuantityValue<Q>,
            this.product.toValue(),
            this.allocation?.toValue() as QuantityValue<Q>?,
        )
    }

    private fun EBioExchange<Q>.toValue(): BioExchangeValue<Q> {
        return BioExchangeValue(
            this.quantity.toValue() as QuantityValue<Q>,
            this.substance.toValue(),
        )
    }

    private fun EImpact<Q>.toValue(): ImpactValue<Q> {
        return ImpactValue(
            this.quantity.toValue() as QuantityValue<Q>,
            this.indicator.toValue(),
        )
    }

    fun EProductSpec<Q>.toValue(): ProductValue<Q> {
        val name = this.name

        @Suppress("UNCHECKED_CAST")
        val referenceUnitValue = (this.referenceUnit as QuantityExpression<Q>?)
            ?.toUnitValue()
            ?: throw EvaluatorException("$this has no reference unit")
        val fromProcessRefValue = this.fromProcess?.toValue()
        return ProductValue(
            name,
            referenceUnitValue,
            fromProcessRefValue,
        )
    }

    fun QuantityExpression<Q>.toUnitValue(): UnitValue<Q> =
        when {
            this is EQuantityScale && this.base is EUnitLiteral ->
                UnitValue(
                    base.symbol.scale(doubleValueOf { scale }),
                    doubleValueOf { scale } * base.scale,
                    base.dimension,
                )

            this is EUnitLiteral ->
                UnitValue(
                    this.symbol,
                    this.scale,
                    this.dimension,
                )

            else -> throw EvaluatorException("$this is not reduced")
        }

    fun ESubstanceSpec<Q>.toValue(): SubstanceValue<Q> {
        @Suppress("UNCHECKED_CAST")
        val referenceUnit = (this.referenceUnit as QuantityExpression<Q>?)
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

    private fun EIndicatorSpec<Q>.toValue(): IndicatorValue<Q> {
        @Suppress("UNCHECKED_CAST")
        val referenceUnit = (this.referenceUnit as QuantityExpression<Q>?)
            ?.toUnitValue()
            ?: throw EvaluatorException("$this has no reference unit")
        return IndicatorValue(
            this.name,
            referenceUnit,
        )
    }


    fun ESubstanceCharacterization<Q>.toValue(): SubstanceCharacterizationValue<Q> {
        return SubstanceCharacterizationValue(
            referenceExchange = this.referenceExchange.toValue(),
            impacts = this.impacts.flatMap { blockToValue(it) { block -> block.toValue() } },
        )
    }

    private fun FromProcess<Q>.toValue(): FromProcessRefValue<Q> {
        return FromProcessRefValue(
            this.name,
            this.matchLabels.elements.mapValues { it.value.toValue() as StringValue },
            this.arguments.mapValues {
                when (val e = it.value) {
                    is QuantityExpression<*> -> e.toValue()
                    is StringExpression -> e.toValue()
                    is ERecord -> RecordValue(e.entries.mapValues { it.value.toValue() })
                    is EDataRef, is ERecordEntry,
                    is EDefaultRecordOf, is ESumProduct, is EFirstRecordOf -> throw EvaluatorException("$it is not " +
                        "reduced")
                }
            },
        )
    }

    fun DataSourceExpression<Q>.toValue(): DataSourceValue<Q> {
        return when(this) {
            is EDataSource -> DataSourceValue(
                this.name,
                this.location,
                this.schema.mapValues { it.value.toValue() },
                this.filter.mapValues { it.value.toValue() },
            )
            else -> throw EvaluatorException("$this is not reduced")
        }
    }

}


