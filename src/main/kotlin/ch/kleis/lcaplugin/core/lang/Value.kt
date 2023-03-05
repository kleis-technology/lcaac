package ch.kleis.lcaplugin.core.lang

sealed interface Value

data class UnitValue(val symbol: String, val scale: Double, val dimension: Dimension) : Value
data class QuantityValue(val amount: Double, val unit: UnitValue) : Value
data class ProductValue(val name: String, val referenceUnit: UnitValue) : Value
data class SubstanceValue(
    val name: String,
    val compartment: String,
    val subcompartment: String?,
    val referenceUnit: UnitValue,
) : Value

data class IndicatorValue(val name: String, val referenceUnit: UnitValue) : Value

sealed interface ExchangeValue : Value
data class TechnoExchangeValue(val quantity: QuantityValue, val product: ProductValue) : ExchangeValue
data class BioExchangeValue(val quantity: QuantityValue, val substance: SubstanceValue) : ExchangeValue
data class ImpactValue(val quantity: QuantityValue, val indicator: IndicatorValue) : ExchangeValue


data class ProcessValue(
    val referenceExchange: TechnoExchangeValue,
    val technosphere: List<TechnoExchangeValue>,
    val biosphere: List<BioExchangeValue>,
) : Value

data class SubstanceCharacterizationValue(
    val referenceExchange: BioExchangeValue,
    val impacts: List<ImpactValue>,
)
