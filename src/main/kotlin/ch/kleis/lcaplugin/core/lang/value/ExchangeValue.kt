package ch.kleis.lcaplugin.core.lang.value

import arrow.optics.optics
import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException

@optics
sealed interface ExchangeValue : Value {
    fun quantity(): QuantityValue

    companion object
}

@optics
data class GenericExchangeValue(
    val quantity: QuantityValue, val port: MatrixColumnIndex
) : ExchangeValue {
    override fun quantity(): QuantityValue {
        return quantity
    }

    companion object
}

@optics
data class TechnoExchangeValue(val quantity: QuantityValue, val product: ProductValue, val allocation: QuantityValue) : ExchangeValue {
    constructor(quantity: QuantityValue, product: ProductValue): this(quantity, product, QuantityValue(100.0, UnitValue("percent", 1.0, Dimension.None)))
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
