package ch.kleis.lcaplugin.core.lang.expression

import arrow.optics.optics

@optics
sealed interface LcaExpression : Expression {
    companion object
}

// Product
@optics
sealed interface LcaProductExpression : LcaExpression {
    companion object
}

@optics
sealed interface LcaUnconstrainedProductExpression : LcaProductExpression {
    companion object
}

@optics
data class EProduct(
    val name: String,
    val referenceUnit: UnitExpression,
) : LcaUnconstrainedProductExpression {
    companion object
}

@optics
data class EProductRef(val name: String) : LcaUnconstrainedProductExpression, RefExpression {
    override fun name(): String {
        return name
    }

    override fun toString(): String = name

    companion object
}

@optics
data class EConstrainedProduct(val product: LcaUnconstrainedProductExpression, val constraint: Constraint) :
    LcaProductExpression {
    companion object
}


// Substance
@optics
sealed interface LcaSubstanceExpression : LcaExpression {
    companion object
}

@optics
data class ESubstance(
    val name: String,
    val compartment: String,
    val subcompartment: String?,
    val referenceUnit: UnitExpression,
) : LcaSubstanceExpression {
    companion object
}

@optics
data class ESubstanceRef(val name: String) : LcaSubstanceExpression, RefExpression {
    override fun name(): String {
        return name
    }

    override fun toString(): String = name

    companion object
}

// Indicator
@optics
sealed interface LcaIndicatorExpression : LcaExpression {
    companion object
}

@optics
data class EIndicator(
    val name: String,
    val referenceUnit: UnitExpression,
) : LcaIndicatorExpression {
    companion object
}

@optics
data class EIndicatorRef(
    val name: String,
) : LcaIndicatorExpression, RefExpression {
    override fun name(): String {
        return name
    }

    override fun toString(): String = name

    companion object
}

// Exchange
@optics
sealed interface LcaExchangeExpression : LcaExpression {
    companion object
}

@optics
data class ETechnoExchange(val quantity: QuantityExpression, val product: LcaProductExpression) :
    LcaExchangeExpression {
    companion object
}

@optics
data class EBioExchange(val quantity: QuantityExpression, val substance: LcaSubstanceExpression) :
    LcaExchangeExpression {
    companion object
}

@optics
data class EImpact(val quantity: QuantityExpression, val indicator: LcaIndicatorExpression) : LcaExchangeExpression {
    companion object
}

// Process
@optics
sealed interface LcaProcessExpression : LcaExpression {
    companion object
}

@optics
data class EProcess(
    val products: List<ETechnoExchange>,
    val inputs: List<ETechnoExchange>,
    val biosphere: List<EBioExchange>,
) : LcaProcessExpression {
    companion object
}

// Substance Characterization
@optics
sealed interface LcaSubstanceCharacterizationExpression : LcaExpression {
    companion object
}

@optics
data class ESubstanceCharacterization(
    val referenceExchange: EBioExchange,
    val impacts: List<EImpact>
) : LcaSubstanceCharacterizationExpression {
    companion object
}

@optics
data class ESubstanceCharacterizationRef(
    val name: String
) : LcaSubstanceCharacterizationExpression, RefExpression {
    override fun name(): String {
        return name
    }

    override fun toString(): String = name

    companion object
}
