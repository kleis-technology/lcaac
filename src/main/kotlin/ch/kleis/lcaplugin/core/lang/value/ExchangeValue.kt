package ch.kleis.lcaplugin.core.lang.value

import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException

sealed interface ExchangeValue : Value {
    fun quantity(): QuantityValue
    fun port(): MatrixColumnIndex
}

data class GenericExchangeValue(
    val quantity: QuantityValue, val port: MatrixColumnIndex
) : ExchangeValue {
    override fun quantity(): QuantityValue {
        return quantity
    }

    override fun port(): MatrixColumnIndex {
        return port
    }
}

data class TechnoExchangeValue(
    val quantity: QuantityValue,
    val product: ProductValue,
    val allocation: QuantityValue? = null,
) : ExchangeValue {
    init {
        if (quantity.unit.dimension != product.referenceUnit.dimension) {
            throw EvaluatorException("incompatible dimensions: ${quantity.unit.dimension} vs ${product.referenceUnit.dimension} for product ${product.name}")
        }
    }

    override fun quantity(): QuantityValue {
        return quantity
    }

    override fun port(): MatrixColumnIndex {
        return product
    }
}

data class BioExchangeValue(val quantity: QuantityValue, val substance: SubstanceValue) : ExchangeValue {
    init {
        if (quantity.unit.dimension != substance.getDimension()) {
            throw EvaluatorException("incompatible dimensions: ${quantity.unit.dimension} vs ${substance.getDimension()} for substance ${substance.getDisplayName()}, quantity=${quantity.amount}")
        }
    }

    override fun quantity(): QuantityValue {
        return quantity
    }

    override fun port(): MatrixColumnIndex {
        return substance
    }
}

data class ImpactValue(val quantity: QuantityValue, val indicator: IndicatorValue) : ExchangeValue {
    init {
        if (quantity.unit.dimension != indicator.referenceUnit.dimension) {
            throw EvaluatorException("incompatible dimensions: ${quantity.unit.dimension} vs ${indicator.referenceUnit.dimension} for indicator ${indicator.name}, quantity=${quantity.amount}")
        }
    }

    override fun quantity(): QuantityValue {
        return quantity
    }

    override fun port(): MatrixColumnIndex {
        return indicator
    }
}
