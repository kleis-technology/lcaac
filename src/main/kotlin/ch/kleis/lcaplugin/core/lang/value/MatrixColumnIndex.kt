package ch.kleis.lcaplugin.core.lang.value

import arrow.optics.optics
import ch.kleis.lcaplugin.core.HasUID
import ch.kleis.lcaplugin.core.lang.Dimension
import ch.kleis.lcaplugin.core.lang.expression.SubstanceType

sealed interface MatrixColumnIndex : Value, HasUID {
    fun getDimension(): Dimension
    fun getDisplayName(): String
    fun referenceUnit(): UnitValue
    override fun getUID(): String {
        return getDisplayName()
    }
}

@optics
data class ProductValue(
    val name: String,
    val referenceUnit: UnitValue,
    val fromProcessRef: FromProcessRefValue? = null
) :
    Value, MatrixColumnIndex {
    override fun getDimension(): Dimension {
        return referenceUnit.dimension
    }

    override fun getDisplayName(): String {
        if (fromProcessRef is FromProcessRefValue) {
            return "$name from ${fromProcessRef.name}${fromProcessRef.arguments}"
        }
        return name
    }

    override fun referenceUnit(): UnitValue {
        return referenceUnit
    }

    fun withFromProcessRef(fromProcessRef: FromProcessRefValue): ProductValue {
        return ProductValue(name, referenceUnit, fromProcessRef)
    }

    companion object
}

@optics
sealed interface SubstanceValue : Value, MatrixColumnIndex {
    companion object
}

@optics
data class PartiallyQualifiedSubstanceValue(
    val name: String,
    val referenceUnit: UnitValue,
) : SubstanceValue {
    override fun getDimension(): Dimension {
        return referenceUnit.dimension
    }

    override fun getDisplayName(): String {
        return name
    }

    override fun referenceUnit(): UnitValue {
        return referenceUnit
    }

    companion object
}

@optics
data class FullyQualifiedSubstanceValue(
    val name: String,
    val type: SubstanceType,
    val compartment: String,
    val subcompartment: String?,
    val referenceUnit: UnitValue,
) : SubstanceValue {
    override fun getDimension(): Dimension {
        return referenceUnit.dimension
    }

    override fun getDisplayName(): String {
        if (compartment.isBlank()) {
            return """[${type.value}] $name"""
        }

        val args = listOfNotNull(
            compartment,
            subcompartment
        ).joinToString()
        return """[${type.value}] $name($args)"""
    }

    override fun referenceUnit(): UnitValue {
        return referenceUnit
    }

    companion object
}

data class IndicatorValue(val name: String, val referenceUnit: UnitValue) : Value, MatrixColumnIndex {
    override fun getDimension(): Dimension {
        return referenceUnit.dimension
    }

    override fun getDisplayName(): String {
        return name
    }

    override fun referenceUnit(): UnitValue {
        return referenceUnit
    }

    companion object
}
