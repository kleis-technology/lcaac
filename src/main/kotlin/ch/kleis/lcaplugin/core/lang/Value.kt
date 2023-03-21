package ch.kleis.lcaplugin.core.lang

import arrow.optics.optics
import ch.kleis.lcaplugin.core.HasUID
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException
import ch.kleis.lcaplugin.core.lang.expression.ConstraintFlag

@optics
sealed interface Value {
    companion object
}

sealed interface ConnectionValue : Value, HasUID
sealed interface PortValue : Value, HasUID {
    fun getDimension(): Dimension
    fun name(): String
    fun referenceUnit(): UnitValue
    override fun getUID(): String {
        return name()
    }
}

@optics
sealed interface ConstraintValue {
    companion object
}


object NoneValue : ConstraintValue

@optics
data class FromProcessRefValue(
    val name: String,
    val arguments: Map<String, QuantityValue>,
    val flag: ConstraintFlag = ConstraintFlag.NONE,
) : ConstraintValue {
    companion object
}

@optics
data class UnitValue(val symbol: String, val scale: Double, val dimension: Dimension) : Value {
    override fun toString(): String {
        return symbol
    }

    companion object
}

@optics
data class QuantityValue(val amount: Double, val unit: UnitValue) : Value {
    fun referenceValue(): Double {
        return amount * unit.scale
    }

    override fun toString(): String {
        return "$amount ${unit.symbol}"
    }

    companion object
}

@optics
data class ProductValue(val name: String, val referenceUnit: UnitValue, val constraint: ConstraintValue = NoneValue) :
    Value, PortValue {
    override fun getDimension(): Dimension {
        return referenceUnit.dimension
    }

    override fun name(): String {
        if (constraint is FromProcessRefValue) {
            return "$name from ${constraint.name}${constraint.arguments}"
        }
        return name
    }

    override fun referenceUnit(): UnitValue {
        return referenceUnit
    }

    fun withConstraint(constraint: ConstraintValue): ProductValue {
        return ProductValue(name, referenceUnit, constraint)
    }

    companion object
}

@optics
data class SubstanceValue(
    val name: String,
    val compartment: String,
    val subcompartment: String?,
    val referenceUnit: UnitValue,
) : Value, PortValue {
    override fun getDimension(): Dimension {
        return referenceUnit.dimension
    }

    override fun name(): String {
        return name
    }

    override fun referenceUnit(): UnitValue {
        return referenceUnit
    }

    companion object
}

@optics
data class IndicatorValue(val name: String, val referenceUnit: UnitValue) : Value, PortValue {
    override fun getDimension(): Dimension {
        return referenceUnit.dimension
    }

    override fun name(): String {
        return name
    }

    override fun referenceUnit(): UnitValue {
        return referenceUnit
    }

    companion object
}

@optics
sealed interface ExchangeValue : Value {
    fun quantity(): QuantityValue

    companion object
}

@optics
data class GenericExchangeValue(
    val quantity: QuantityValue, val port: PortValue
) : ExchangeValue {
    override fun quantity(): QuantityValue {
        return quantity
    }

    companion object
}

@optics
data class TechnoExchangeValue(val quantity: QuantityValue, val product: ProductValue) : ExchangeValue {
    init {
        if (quantity.unit.dimension != product.referenceUnit.dimension) {
            throw EvaluatorException("incompatible dimensions: ${quantity.unit.dimension} vs ${product.referenceUnit.dimension}")
        }
    }

    override fun quantity(): QuantityValue {
        return quantity
    }

    companion object
}

@optics
data class BioExchangeValue(val quantity: QuantityValue, val substance: SubstanceValue) : ExchangeValue {
    init {
        if (quantity.unit.dimension != substance.referenceUnit.dimension) {
            throw EvaluatorException("incompatible dimensions: ${quantity.unit.dimension} vs ${substance.referenceUnit.dimension}")
        }
    }

    override fun quantity(): QuantityValue {
        return quantity
    }

    companion object
}

@optics
data class ImpactValue(val quantity: QuantityValue, val indicator: IndicatorValue) : ExchangeValue {
    init {
        if (quantity.unit.dimension != indicator.referenceUnit.dimension) {
            throw EvaluatorException("incompatible dimensions: ${quantity.unit.dimension} vs ${indicator.referenceUnit.dimension}")
        }
    }

    override fun quantity(): QuantityValue {
        return quantity
    }

    companion object
}


@optics
data class ProcessValue(
    val name: String,
    val products: List<TechnoExchangeValue>,
    val inputs: List<TechnoExchangeValue>,
    val biosphere: List<BioExchangeValue>,
) : Value, ConnectionValue {
    companion object
}

@optics
data class SubstanceCharacterizationValue(
    val referenceExchange: BioExchangeValue,
    val impacts: List<ImpactValue>,
) : Value, ConnectionValue {
    companion object
}

@optics
data class SystemValue(
    val processes: List<ProcessValue>,
    val substanceCharacterizations: List<SubstanceCharacterizationValue>,
) : Value, HasUID {
    companion object
}

@optics
data class CharacterizationFactorValue(
    val output: ExchangeValue,
    val input: ExchangeValue,
) : Value {
    companion object
}
