package ch.kleis.lcaplugin.core.lang.expression

import ch.kleis.lcaplugin.core.lang.Constraint

sealed interface LcaExpression : Expression

// Product
sealed interface LcaProductExpression : LcaExpression
sealed interface LcaUnconstrainedProductExpression : LcaProductExpression

data class EProduct(
    val name: String,
    val referenceUnit: UnitExpression,
) : LcaUnconstrainedProductExpression
data class EProductRef(val name: String) : LcaUnconstrainedProductExpression, RefExpression {
    override fun name(): String {
        return name
    }

    override fun toString(): String = name
}

data class EConstrainedProduct(val product: LcaUnconstrainedProductExpression, val constraint: Constraint):
    LcaProductExpression



// Substance
sealed interface LcaSubstanceExpression : LcaExpression
data class ESubstance(
    val name: String,
    val compartment: String,
    val subcompartment: String?,
    val referenceUnit: UnitExpression,
) : LcaSubstanceExpression
data class ESubstanceRef(val name: String) : LcaSubstanceExpression, RefExpression {
    override fun name(): String {
        return name
    }

    override fun toString(): String = name
}

// Indicator
sealed interface LcaIndicatorExpression : LcaExpression
data class EIndicator(
    val name: String,
    val referenceUnit: UnitExpression,
) : LcaIndicatorExpression
data class EIndicatorRef(
    val name: String,
) : LcaIndicatorExpression, RefExpression {
    override fun name(): String {
        return name
    }

    override fun toString(): String = name
}

// Exchange
sealed interface LcaExchangeExpression : LcaExpression
data class ETechnoExchange(val quantity: QuantityExpression, val product: LcaProductExpression) : LcaExchangeExpression
data class EBioExchange(val quantity: QuantityExpression, val substance: LcaSubstanceExpression) : LcaExchangeExpression
data class EImpact(val quantity: QuantityExpression, val indicator: LcaIndicatorExpression) : LcaExchangeExpression

// Process
sealed interface LcaProcessExpression : LcaExpression
data class EProcess(
    val products: List<ETechnoExchange>,
    val inputs: List<ETechnoExchange>,
    val biosphere: List<EBioExchange>,
) : LcaProcessExpression

// Substance Characterization
sealed interface LcaSubstanceCharacterizationExpression : LcaExpression
data class ESubstanceCharacterization(
    val referenceExchange: EBioExchange,
    val impacts: List<EImpact>
) : LcaSubstanceCharacterizationExpression
data class ESubstanceCharacterizationRef(
    val name: String
) : LcaSubstanceCharacterizationExpression, RefExpression {
    override fun name(): String {
        return name
    }

    override fun toString(): String = name
}

