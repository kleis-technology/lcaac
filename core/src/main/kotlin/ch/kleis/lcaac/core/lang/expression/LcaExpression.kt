package ch.kleis.lcaac.core.lang.expression

import arrow.optics.optics
import ch.kleis.lcaac.core.lang.evaluator.EvaluatorException

sealed interface LcaExpression<Q> {
    companion object
}

sealed interface PortExpression<Q>
sealed interface ConnectionExpression<Q>

// Product
@optics
data class EProductSpec<Q>(
    val name: String,
    val referenceUnit: DataExpression<Q>? = null,
    val fromProcess: FromProcess<Q>? = null,
) : LcaExpression<Q>, PortExpression<Q> {
    companion object
}

// Substance
enum class SubstanceType(val value: String) {
    EMISSION("Emission"), RESOURCE("Resource"), LAND_USE("Land_use");

    companion object {
        private val map = entries.associateBy { it.value }
        infix fun of(value: String): SubstanceType =
            map[value] ?: throw EvaluatorException("Invalid SubstanceType: $value")
    }

    override fun toString(): String {
        return value
    }


}

@optics
data class ESubstanceSpec<Q>(
    val name: String,
    val displayName: String = name,
    val type: SubstanceType? = null,
    val compartment: String? = null,
    val subCompartment: String? = null,
    val referenceUnit: DataExpression<Q>? = null,
) : LcaExpression<Q>, PortExpression<Q> {
    companion object
}

// Indicator
@optics
data class EIndicatorSpec<Q>(
    val name: String,
    val referenceUnit: DataExpression<Q>? = null,
) : LcaExpression<Q>, PortExpression<Q> {
    companion object
}

// Exchange
@optics
sealed interface LcaExchangeExpression<Q> : LcaExpression<Q> {
    val quantity: DataExpression<Q>

    companion object
}

@optics
data class ETechnoExchange<Q>(
    override val quantity: DataExpression<Q>,
    val product: EProductSpec<Q>,
    val allocation: DataExpression<Q>? = null,
) : LcaExchangeExpression<Q> {
    companion object
}

@optics
data class EBioExchange<Q>(override val quantity: DataExpression<Q>, val substance: ESubstanceSpec<Q>) :
    LcaExchangeExpression<Q> {
    companion object
}

@optics
data class EImpact<Q>(override val quantity: DataExpression<Q>, val indicator: EIndicatorSpec<Q>) :
    LcaExchangeExpression<Q> {
    companion object
}

// Process
@optics
data class EProcess<Q>(
    val name: String,
    val labels: Map<String, EStringLiteral<Q>> = emptyMap(),
    val products: List<ETechnoExchange<Q>> = emptyList(),
    val inputs: List<ETechnoExchange<Q>> = emptyList(),
    val biosphere: List<EBioExchange<Q>> = emptyList(),
    val impacts: List<EImpact<Q>> = emptyList(),
) : LcaExpression<Q>, ConnectionExpression<Q> {
    companion object
}

// Substance Characterization
@optics
data class ESubstanceCharacterization<Q>(
    val referenceExchange: EBioExchange<Q>,
    val impacts: List<EImpact<Q>>
) : LcaExpression<Q>, ConnectionExpression<Q> {
    companion object

    fun hasImpacts(): Boolean {
        return impacts.isNotEmpty()
    }
}

