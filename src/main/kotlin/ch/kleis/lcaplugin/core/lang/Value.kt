package ch.kleis.lcaplugin.core.lang

import ch.kleis.lcaplugin.core.HasUID
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException

sealed interface Value

sealed interface ConnectionValue : Value, HasUID
sealed interface PortValue : Value, HasUID {
    fun getDimension(): Dimension
    fun name(): String
    fun referenceUnit(): UnitValue
}

data class UnitValue(val symbol: String, val scale: Double, val dimension: Dimension) : Value
data class QuantityValue(val amount: Double, val unit: UnitValue) : Value {
    fun referenceValue(): Double {
        return amount * unit.scale
    }
}

data class ProductValue(val name: String, val referenceUnit: UnitValue) : Value, PortValue {
    override fun getDimension(): Dimension {
        return referenceUnit.dimension
    }

    override fun name(): String {
        return name
    }

    override fun referenceUnit(): UnitValue {
        return referenceUnit
    }
}

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
}

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
}

sealed interface ExchangeValue : Value {
    fun quantity(): QuantityValue
}
data class GenericExchangeValue(
    val quantity: QuantityValue, val port: PortValue
) : ExchangeValue {
    override fun quantity(): QuantityValue {
        return quantity
    }
}

data class TechnoExchangeValue(val quantity: QuantityValue, val product: ProductValue) : ExchangeValue {
    init {
        if (quantity.unit.dimension != product.referenceUnit.dimension) {
            throw EvaluatorException("incompatible dimensions: ${quantity.unit.dimension} vs ${product.referenceUnit.dimension}")
        }
    }
    override fun quantity(): QuantityValue {
        return quantity
    }
}

data class BioExchangeValue(val quantity: QuantityValue, val substance: SubstanceValue) : ExchangeValue {
    init {
        if (quantity.unit.dimension != substance.referenceUnit.dimension) {
            throw EvaluatorException("incompatible dimensions: ${quantity.unit.dimension} vs ${substance.referenceUnit.dimension}")
        }
    }

    override fun quantity(): QuantityValue {
        return quantity
    }
}

data class ImpactValue(val quantity: QuantityValue, val indicator: IndicatorValue) : ExchangeValue {
    init {
        if (quantity.unit.dimension != indicator.referenceUnit.dimension) {
            throw EvaluatorException("incompatible dimensions: ${quantity.unit.dimension} vs ${indicator.referenceUnit.dimension}")
        }
    }

    override fun quantity(): QuantityValue {
        return quantity
    }
}


data class ProcessValue(
    val products: List<TechnoExchangeValue>,
    val inputs: List<TechnoExchangeValue>,
    val biosphere: List<BioExchangeValue>,
) : Value, ConnectionValue

data class SubstanceCharacterizationValue(
    val referenceExchange: BioExchangeValue,
    val impacts: List<ImpactValue>,
) : Value, ConnectionValue

data class SystemValue(
    val processes: List<ProcessValue>,
    val substanceCharacterizations: List<SubstanceCharacterizationValue>,
) : Value, HasUID

data class CharacterizationFactorValue(
    val output: ExchangeValue,
    val input: ExchangeValue,
) : Value
