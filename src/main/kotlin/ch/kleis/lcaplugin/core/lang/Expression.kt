package ch.kleis.lcaplugin.core.lang

sealed interface Expression

/*
    Unit
 */
sealed interface UnitExpression : Expression
data class EUnitLiteral(val symbol: String, val scale: Double, val dimension: Dimension) : UnitExpression
data class EUnitMul(val left: UnitExpression, val right: UnitExpression) : UnitExpression
data class EUnitDiv(val left: UnitExpression, val right: UnitExpression) : UnitExpression
data class EUnitPow(val unit: UnitExpression, val exponent: Double) : UnitExpression
data class EUnitRef(val name: String) : UnitExpression


/*
    Quantity
 */
sealed interface QuantityExpression : Expression
data class EQuantityLiteral(val amount: Double, val unit: UnitExpression) : QuantityExpression
data class EQuantityAdd(val left: QuantityExpression, val right: QuantityExpression) : QuantityExpression
data class EQuantitySub(val left: QuantityExpression, val right: QuantityExpression) : QuantityExpression
data class EQuantityMul(val left: QuantityExpression, val right: QuantityExpression) : QuantityExpression
data class EQuantityDiv(val left: QuantityExpression, val right: QuantityExpression) : QuantityExpression
data class EQuantityPow(val quantity: QuantityExpression, val exponent: Double) : QuantityExpression
data class EQuantityRef(val name: String): QuantityExpression

/*
    LCA Modeling
 */
sealed interface LcaExpression : Expression


// Product
sealed interface LcaProductExpression : LcaExpression

sealed interface LcaUnconstrainedProductExpression : LcaProductExpression

data class EProduct(
    val name: String,
    val referenceUnit: UnitExpression,
) : LcaUnconstrainedProductExpression
data class EProductRef(val name: String) : LcaUnconstrainedProductExpression

data class EConstrainedProduct(val product: LcaUnconstrainedProductExpression, val constraint: Constraint): LcaProductExpression



// Substance
sealed interface LcaSubstanceExpression : LcaExpression
data class ESubstance(
    val name: String,
    val compartment: String,
    val subcompartment: String?,
    val referenceUnit: UnitExpression,
) : LcaSubstanceExpression
data class ESubstanceRef(val name: String) : LcaSubstanceExpression

// Indicator
sealed interface LcaIndicatorExpression : LcaExpression
data class EIndicator(
    val name: String,
    val referenceUnit: UnitExpression,
) : LcaIndicatorExpression
data class EIndicatorRef(
    val name: String,
) : LcaIndicatorExpression

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

data class EProcessRef(val name: String) : LcaProcessExpression

// Substance Characterization
sealed interface LcaSubstanceCharacterizationExpression : LcaExpression
data class ESubstanceCharacterization(
    val referenceExchange: EBioExchange,
    val impacts: List<EImpact>
) : LcaSubstanceCharacterizationExpression

