package ch.kleis.lcaplugin.core.lang.expression

import arrow.optics.optics
import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.evaluator.EvaluatorException

@optics
sealed interface LcaExpression : Expression {
    companion object
}

// Product
@optics
data class EProductSpec(
    val name: String,
    val referenceUnit: UnitExpression? = null,
    val fromProcessRef: FromProcessRef? = null,
) : LcaExpression {
    companion object
}

// Substance
enum class SubstanceType(val value: String) { // TODO Undefined because of ReduceAndComplete.completeSubstances(), to solve
    EMISSION("Emission"), RESOURCE("Resource"), LAND_USE("Land_use"), UNDEFINED("Undefined");

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
    val referenceUnit: UnitExpression? = null,
) : LcaExpression {
    companion object
}

// Indicator
@optics
data class EIndicatorSpec(
    val name: String,
    val referenceUnit: UnitExpression? = null,
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
    val quantity: QuantityExpression,
    val product: EProductSpec,
    val allocation: QuantityExpression
) :
    LcaExchangeExpression {
    constructor(quantity: QuantityExpression, product: EProductSpec) : this(
        quantity,
        product,
        EQuantityLiteral(100.0, EUnitLiteral("percent", 0.01, Dimension.None))
    )

    companion object
}

@optics
data class EBioExchange(val quantity: QuantityExpression, val substance: ESubstanceSpec) :
    LcaExchangeExpression {
    companion object
}

@optics
data class EImpact(val quantity: QuantityExpression, val indicator: EIndicatorSpec) : LcaExchangeExpression {
    companion object
}

// Process
@optics
data class EProcess(
    val name: String,
    val products: List<ETechnoExchange>,
    val inputs: List<ETechnoExchange>,
    val biosphere: List<EBioExchange>,
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

