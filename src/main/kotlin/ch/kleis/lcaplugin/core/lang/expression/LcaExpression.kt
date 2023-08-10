package ch.kleis.lcaplugin.core.lang.expression

import arrow.optics.optics
import ch.kleis.lcaplugin.core.lang.dimension.Dimension
import ch.kleis.lcaplugin.core.lang.dimension.UnitSymbol
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException

@optics
sealed interface LcaExpression : Expression {
    companion object
}

// Product
@optics
data class EProductSpec(
    val name: String,
    val referenceUnit: DataExpression? = null,
    val fromProcess: FromProcess? = null,
) : LcaExpression {
    companion object
}

// Substance
enum class SubstanceType(val value: String) {
    EMISSION("Emission"), RESOURCE("Resource"), LAND_USE("Land_use");

    companion object {
        private val map = SubstanceType.values().associateBy { it.value }
        infix fun of(value: String): SubstanceType =
            map[value] ?: throw EvaluatorException("Invalid SubstanceType: $value")
    }

    override fun toString(): String {
        return value
    }


}

@optics
data class ESubstanceSpec(
    val name: String,
    val displayName: String = name,
    val type: SubstanceType? = null,
    val compartment: String? = null,
    val subCompartment: String? = null,
    val referenceUnit: DataExpression? = null,
) : LcaExpression {
    companion object
}

// Indicator
@optics
data class EIndicatorSpec(
    val name: String,
    val referenceUnit: DataExpression? = null,
) : LcaExpression {
    companion object
}

// Exchange
@optics
sealed interface LcaExchangeExpression : LcaExpression {
    companion object
}

@optics
data class ETechnoExchange(
    val quantity: DataExpression,
    val product: EProductSpec,
    val allocation: DataExpression
) :
    LcaExchangeExpression {
    constructor(quantity: DataExpression, product: EProductSpec) : this(
        quantity,
        product,
        EQuantityScale(100.0, EUnitLiteral(UnitSymbol.of("percent"), 0.01, Dimension.None))
    )

    companion object
}

@optics
data class EBioExchange(val quantity: DataExpression, val substance: ESubstanceSpec) :
    LcaExchangeExpression {
    companion object
}

@optics
data class EImpact(val quantity: DataExpression, val indicator: EIndicatorSpec) : LcaExchangeExpression {
    companion object
}

// Process
@optics
data class EProcess(
    val name: String,
    val labels: Map<String, EStringLiteral> = emptyMap(),
    val products: List<ETechnoExchange> = emptyList(),
    val inputs: List<ETechnoExchange> = emptyList(),
    val biosphere: List<EBioExchange> = emptyList(),
    val impacts: List<EImpact> = emptyList(),
) : LcaExpression {
    companion object
}

// Substance Characterization
@optics
data class ESubstanceCharacterization(
    val referenceExchange: EBioExchange,
    val impacts: List<EImpact>
) : LcaExpression {
    fun hasImpacts(): Boolean {
        return impacts.isNotEmpty()
    }

    companion object
}

